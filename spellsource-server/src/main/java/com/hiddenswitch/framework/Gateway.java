package com.hiddenswitch.framework;

import com.be_hase.grpc.micrometer.MicrometerServerInterceptor;
import com.google.common.collect.Streams;
import com.netflix.concurrency.limits.grpc.server.ConcurrencyLimitServerInterceptor;
import com.netflix.concurrency.limits.grpc.server.GrpcServerLimiterBuilder;
import com.netflix.concurrency.limits.limit.Gradient2Limit;
import com.netflix.concurrency.limits.limit.WindowedLimit;
import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.micrometer.core.instrument.Metrics;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Closeable;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gateway extends AbstractVerticle {
	private static Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
	List<Closeable> closeables = new ArrayList<>();

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		var configuration = Environment.getConfiguration();
		CompositeFuture.all(
						Legacy.services(),
						Legacy.unauthenticatedCards(),
						Matchmaking.services(),
						Accounts.unauthenticatedService(),
						Accounts.authenticatedService(),
						Games.services())
				.compose(services -> {
					var server = GrpcServer.server(vertx);
					var httpServer = vertx.createHttpServer(new HttpServerOptions()
									.setIdleTimeout(Integer.MAX_VALUE))
							.requestHandler(server)
							.listen(grpcPort());

					var list = services.list();
					for (var service : list) {
						var boundService = service instanceof BindableService ? ((BindableService) service).bindService() : (ServerServiceDefinition) service;
						// basic interceptors for grpc
						var interceptors = Stream.of(new MicrometerServerInterceptor(Metrics.globalRegistry),
								TracingServerInterceptor
										.newBuilder()
										.withTracer(GlobalTracer.get())
										.withStreaming()
										.withVerbosity()
										.build());
						var limiter = Stream.<ServerInterceptor>empty();
						if (configuration.hasRateLimiter() && configuration.getRateLimiter().getEnabled()) {
							limiter = Stream.of(ConcurrencyLimitServerInterceptor.newBuilder(
									new GrpcServerLimiterBuilder()
											.partitionResolver(context -> {
												var address = context.getCall().getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
												if (address instanceof InetSocketAddress) {
													var inetSocketAddress = (InetSocketAddress) address;
													var hostAddress = inetSocketAddress.getAddress().getHostAddress();
													if (hostAddress.equals("127.0.0.1")) {
														hostAddress += ":" + inetSocketAddress.getPort();
													}
													return hostAddress;
												}

												return address.toString();
											})
											.limit(WindowedLimit.newBuilder()
													.build(Gradient2Limit.newBuilder()
															.build()))
											.build()).build());
						}
						boundService = ServerInterceptors.intercept(boundService, Streams.concat(interceptors, limiter).collect(Collectors.toList()));
						GrpcServiceBridge.bridge(boundService).bind(server);
					}

					// include reflection
					GrpcServiceBridge.bridge(ProtoReflectionService.newInstance()).bind(server);

					/*
					nettyServerBuilder
							.maxConnectionIdle(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
							.maxConnectionAge(29, TimeUnit.DAYS)
							.keepAliveTime(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeMillis(), TimeUnit.MILLISECONDS)
							.keepAliveTimeout(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeoutMillis(), TimeUnit.MILLISECONDS)
							.permitKeepAliveTime(Math.max(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeMillis() - 400, 100), TimeUnit.MILLISECONDS)
							.permitKeepAliveWithoutCalls(serverConfiguration.getGrpcConfiguration().getServerPermitKeepAliveWithoutCalls());
					 */
					return httpServer
							.onSuccess(listening -> closeables.add(listening::close))
							.map((Void) null);
				})
				.onFailure(Environment.onFailure())
				.onComplete(startPromise);
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		CompositeFuture.all(closeables.stream().map(closeable -> {
					var promise = Promise.<Void>promise();
					closeable.close(promise);
					return promise.future();
				}).collect(Collectors.toList()))
				.map((Void) null)
				.onComplete(stopPromise);
	}

	public static int grpcPort() {
		return Environment.getConfiguration().getGrpcConfiguration().getPort();
	}
}
