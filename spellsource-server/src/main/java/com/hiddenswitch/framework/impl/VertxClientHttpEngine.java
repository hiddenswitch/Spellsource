package com.hiddenswitch.framework.impl;

import io.vertx.await.Async;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.engines.AsyncClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.FinalizedClientResponse;
import org.jboss.resteasy.tracing.RESTEasyTracingLogger;
import org.jboss.resteasy.util.CaseInsensitiveMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * A Vert.x 5 {@link AsyncClientHttpEngine} implementation for RESTEasy that uses a Vert.x {@link HttpClient}
 * and suspends virtual threads cleanly via {@link Async#await(Future)} when invoked on a Vert.x virtual thread.
 */
public class VertxClientHttpEngine implements AsyncClientHttpEngine {
	public static final String REQUEST_TIMEOUT_MS = Vertx.class + ".RequestTimeoutMs";

	private final Vertx vertx;
	private final boolean ownsVertx;
	private final HttpClient httpClient;
	private volatile boolean followRedirects;

	public VertxClientHttpEngine() {
		this(Vertx.vertx(), new HttpClientOptions(), true);
	}

	public VertxClientHttpEngine(Vertx vertx) {
		this(vertx, new HttpClientOptions(), false);
	}

	public VertxClientHttpEngine(Vertx vertx, HttpClientOptions options) {
		this(vertx, options, false);
	}

	private VertxClientHttpEngine(Vertx vertx, HttpClientOptions options, boolean ownsVertx) {
		this.vertx = vertx;
		this.ownsVertx = ownsVertx;
		this.httpClient = vertx.createHttpClient(options);
	}

	public VertxClientHttpEngine(HttpClient httpClient) {
		this.vertx = null;
		this.ownsVertx = false;
		this.httpClient = httpClient;
	}

	@Override
	public SSLContext getSslContext() {
		return null;
	}

	@Override
	public HostnameVerifier getHostnameVerifier() {
		return null;
	}

	@Override
	public boolean isFollowRedirects() {
		return followRedirects;
	}

	@Override
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	@Override
	public Response invoke(Invocation invocation) {
		var clientInvocation = (ClientInvocation) invocation;
		var future = send(clientInvocation);
		try {
			if (Thread.currentThread().isVirtual() && Vertx.currentContext() != null) {
				return Async.await(future);
			}
			return future.toCompletionStage().toCompletableFuture().get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw clientException(e, null);
		} catch (ExecutionException e) {
			throw clientException(e.getCause(), null);
		} catch (Throwable t) {
			throw clientException(t, null);
		}
	}

	@Override
	public <T> java.util.concurrent.Future<T> submit(
			ClientInvocation request,
			boolean buffered,
			InvocationCallback<T> callback,
			ResultExtractor<T> extractor) {
		CompletableFuture<T> cf = submit(request, buffered, extractor, null);
		if (callback != null) {
			cf = cf.whenComplete((result, throwable) -> {
				if (throwable != null) {
					callback.failed(throwable);
				} else {
					callback.completed(result);
				}
			});
		}
		return cf;
	}

	@Override
	public <T> CompletableFuture<T> submit(
			ClientInvocation request,
			boolean buffered,
			ResultExtractor<T> extractor,
			ExecutorService executorService) {
		CompletableFuture<ClientResponse> responseCf = send(request).toCompletionStage().toCompletableFuture();
		if (executorService == null) {
			return responseCf.thenApply(response -> {
				try {
					return extractor.extractResult(response);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return responseCf.thenApplyAsync(response -> {
			try {
				return extractor.extractResult(response);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, executorService);
	}

	public Future<ClientResponse> send(ClientInvocation request) {
		HttpMethod method = HttpMethod.valueOf(request.getMethod());
		Buffer bodyBuffer;
		if (request.getEntity() != null) {
			bodyBuffer = Buffer.buffer(requestContent(request));
		} else {
			bodyBuffer = null;
		}

		RequestOptions options = new RequestOptions();
		options.setMethod(method);
		options.setFollowRedirects(followRedirects);

		MultiMap headers = MultiMap.caseInsensitiveMultiMap();
		request.getHeaders().asMap().forEach(headers::add);
		if (bodyBuffer != null) {
			headers.set(HttpHeaders.CONTENT_LENGTH, Integer.toString(bodyBuffer.length()));
		}
		if (!headers.contains(HttpHeaders.USER_AGENT)) {
			headers.set(HttpHeaders.USER_AGENT, "Vertx");
		}
		options.setHeaders(headers);

		URI uri = request.getUri();
		options.setHost(uri.getHost());
		boolean ssl = "https".equalsIgnoreCase(uri.getScheme());
		options.setSsl(ssl);
		if (uri.getPort() == -1) {
			options.setPort(ssl ? 443 : 80);
		} else {
			options.setPort(uri.getPort());
		}

		String relativeUri = uri.getRawPath();
		if (uri.getRawQuery() != null && !uri.getRawQuery().trim().isEmpty()) {
			relativeUri = relativeUri + "?" + uri.getRawQuery();
		}
		options.setURI(relativeUri);

		if (request.getConfiguration().hasProperty(REQUEST_TIMEOUT_MS)) {
			long timeoutMs = unwrapTimeout(request.getConfiguration().getProperty(REQUEST_TIMEOUT_MS));
			if (timeoutMs > 0) {
				options.setTimeout(timeoutMs);
			}
		}

		return httpClient.request(options)
				.compose(httpRequest -> bodyBuffer != null ? httpRequest.send(bodyBuffer) : httpRequest.send())
				.compose(httpResponse -> httpResponse.body()
						.map(responseBuffer -> toRestEasyResponse(request.getClientConfiguration(), httpResponse, responseBuffer)));
	}

	private static long unwrapTimeout(Object timeout) {
		if (timeout instanceof Duration duration) {
			return duration.toMillis();
		} else if (timeout instanceof Number number) {
			return number.longValue();
		} else if (timeout != null) {
			return Long.parseLong(timeout.toString());
		}
		return -1L;
	}

	private static byte[] requestContent(ClientInvocation request) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		request.getDelegatingOutputStream().setDelegate(baos);
		try {
			request.writeRequestBody(request.getEntityStream());
			baos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write the request body!", e);
		}
	}

	private static ClientResponse toRestEasyResponse(
			ClientConfiguration clientConfiguration,
			HttpClientResponse response,
			Buffer bodyBuffer) {
		byte[] bytes = bodyBuffer != null ? bodyBuffer.getBytes() : new byte[0];
		InputStream initialStream = new ByteArrayInputStream(bytes);
		ClientResponse clientResponse = new FinalizedClientResponse(clientConfiguration, RESTEasyTracingLogger.empty()) {
			private InputStream is = initialStream;

			@Override
			protected InputStream getInputStream() {
				return is;
			}

			@Override
			protected void setInputStream(InputStream newIs) {
				this.is = newIs;
			}

			@Override
			public void releaseConnection() throws IOException {
				releaseConnection(false);
			}

			@Override
			public void releaseConnection(boolean consumeInputStream) throws IOException {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ignored) {
					}
				}
			}
		};
		clientResponse.setStatus(response.statusCode());
		CaseInsensitiveMap<String> headers = new CaseInsensitiveMap<>();
		response.headers().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));
		clientResponse.setHeaders(headers);
		return clientResponse;
	}

	static RuntimeException clientException(Throwable ex, Response clientResponse) {
		if (ex == null) {
			return new ProcessingException(new NullPointerException());
		} else if (ex instanceof WebApplicationException webAppEx) {
			return webAppEx;
		} else if (ex instanceof ProcessingException procEx) {
			return procEx;
		} else if (clientResponse != null) {
			return new ResponseProcessingException(clientResponse, ex);
		} else {
			return new ProcessingException(ex);
		}
	}

	@Override
	public void close() {
		if (ownsVertx && vertx != null) {
			vertx.close();
		} else if (httpClient != null) {
			httpClient.close();
		}
	}
}
