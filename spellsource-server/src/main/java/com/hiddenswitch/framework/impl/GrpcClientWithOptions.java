package com.hiddenswitch.framework.impl;


import io.grpc.MethodDescriptor;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.Address;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientOptions;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.common.ServiceMethod;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientWithOptions implements GrpcClient {

	private final Vertx vertx;
	private final GrpcClient delegate;
	private RequestOptions requestOptions = new RequestOptions();

	public GrpcClientWithOptions(Vertx vertx, HttpClientOptions options) {
		this.vertx = vertx;
		this.delegate = GrpcClient.client(vertx,
				new GrpcClientOptions().setMaxMessageSize(8 * 1024 * 1024),
				new HttpClientOptions(options).setProtocolVersion(HttpVersion.HTTP_2));
	}

	public GrpcClientWithOptions(Vertx vertx) {
		this(vertx, new HttpClientOptions().setHttp2ClearTextUpgrade(false));
	}

	private <Req, Resp> Future<GrpcClientRequest<Req, Resp>> applyHeaders(Future<GrpcClientRequest<Req, Resp>> fut) {
		MultiMap headers = requestOptions.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			return fut.map(req -> {
				MultiMap reqHeaders = req.headers();
				for (var entry : headers) {
					reqHeaders.add(entry.getKey(), entry.getValue());
				}
				return req;
			});
		}
		return fut;
	}

	@Override
	public Future<GrpcClientRequest<Buffer, Buffer>> request(Address server) {
		return applyHeaders(delegate.request(server));
	}

	@Override
	public Future<GrpcClientRequest<Buffer, Buffer>> request() {
		return applyHeaders(delegate.request());
	}

	@Override
	public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(Address server, ServiceMethod<Resp, Req> method) {
		return applyHeaders(delegate.request(server, method));
	}

	@Override
	public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(ServiceMethod<Resp, Req> method) {
		return applyHeaders(delegate.request(method));
	}

	@Override
	public Future<Void> close() {
		return delegate.close();
	}

	public RequestOptions requestOptions() {
		return requestOptions;
	}

	public GrpcClientWithOptions setRequestOptions(RequestOptions requestOptions) {
		this.requestOptions = requestOptions;
		return this;
	}
}
