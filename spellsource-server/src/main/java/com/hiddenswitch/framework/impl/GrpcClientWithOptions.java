package com.hiddenswitch.framework.impl;


import io.grpc.MethodDescriptor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.client.impl.GrpcClientRequestImpl;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientWithOptions implements GrpcClient {

	private final Vertx vertx;
	private HttpClient client;
	private RequestOptions requestOptions = new RequestOptions();

	public GrpcClientWithOptions(Vertx vertx, HttpClientOptions options) {
		this.vertx = vertx;
		this.client = vertx.createHttpClient(new HttpClientOptions(options)
				.setProtocolVersion(HttpVersion.HTTP_2));
	}

	public GrpcClientWithOptions(Vertx vertx) {
		this(vertx, new HttpClientOptions().setHttp2ClearTextUpgrade(false));
	}

	@Override
	public Future<GrpcClientRequest<Buffer, Buffer>> request(SocketAddress server) {
		RequestOptions options = new RequestOptions(requestOptions)
				.setMethod(HttpMethod.POST)
				.setServer(server);
		return client.request(options)
				.map(request -> new GrpcClientRequestImpl<>(request, GrpcMessageEncoder.IDENTITY, GrpcMessageDecoder.IDENTITY));
	}

	@Override
	public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(SocketAddress server, MethodDescriptor<Req, Resp> service) {
		RequestOptions options = new RequestOptions(requestOptions)
				.setMethod(HttpMethod.POST)
				.setServer(server);
		GrpcMessageDecoder<Resp> messageDecoder = GrpcMessageDecoder.unmarshaller(service.getResponseMarshaller());
		GrpcMessageEncoder<Req> messageEncoder = GrpcMessageEncoder.marshaller(service.getRequestMarshaller());
		return client.request(options)
				.map(request -> {
					GrpcClientRequestImpl<Req, Resp> call = new GrpcClientRequestImpl<>(request, messageEncoder, messageDecoder);
					call.fullMethodName(service.getFullMethodName());
					return call;
				});
	}

	@Override
	public Future<Void> close() {
		return client.close();
	}

	public RequestOptions requestOptions() {
		return requestOptions;
	}

	public GrpcClientWithOptions setRequestOptions(RequestOptions requestOptions) {
		this.requestOptions = requestOptions;
		return this;
	}
}
