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
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gateway extends AbstractVerticle {
	private static Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
	VertxServer server;

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
					var serverConfiguration = Environment.getConfiguration();
					var builder = VertxServerBuilder.forPort(vertx, grpcPort());

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
						builder.addService(boundService);
					}

					// include reflection
					builder.addService(ProtoReflectionService.newInstance());

					var nettyServerBuilder = builder.nettyBuilder();
					nettyServerBuilder
							.keepAliveTime(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeMillis(), TimeUnit.MILLISECONDS)
							.keepAliveTimeout(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeoutMillis(), TimeUnit.MILLISECONDS)
							.permitKeepAliveWithoutCalls(serverConfiguration.getGrpcConfiguration().getServerPermitKeepAliveWithoutCalls());
					return Future.succeededFuture(builder);
				})
				.compose(builder -> {
					var promise = Promise.<Void>promise();
					server = builder.build();
					server.start(promise);
					return promise.future();
				})
				.onFailure(Environment.onFailure())
				.onComplete(startPromise);
	}

	public static int grpcPort() {
		return Environment.getConfiguration().getGrpcConfiguration().getPort();
	}
}
