package com.hiddenswitch.framework;

import com.be_hase.grpc.micrometer.MicrometerServerInterceptor;
import io.grpc.BindableService;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.micrometer.core.instrument.Metrics;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

import java.util.concurrent.TimeUnit;

public class Gateway extends AbstractVerticle {
	VertxServer server;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		CompositeFuture.all(
				Legacy.services(),
				Legacy.unauthenticatedCards(),
				Matchmaking.services(),
				Accounts.unauthenticatedService(),
				Accounts.authenticatedService(),
				Games.services())
				.compose(services -> {
					var serverConfiguration = Environment.cachedConfigurationOrGet();
					var builder = VertxServerBuilder.forPort(vertx, grpcPort());

					var list = services.list();
					for (var service : list) {
						var boundService = service instanceof BindableService ? ((BindableService) service).bindService() : (ServerServiceDefinition) service;
						// basic interceptors for grpc
						boundService = ServerInterceptors.intercept(boundService,
								new MicrometerServerInterceptor(Metrics.globalRegistry),
								TracingServerInterceptor
										.newBuilder()
										.withTracer(GlobalTracer.get())
										.withStreaming()
										.withVerbosity()
										.build());
						builder.addService(boundService);
					}
					var nettyServerBuilder = builder.nettyBuilder();
					nettyServerBuilder.keepAliveTime(serverConfiguration.getGrpcConfiguration().getServerKeepAliveTimeMillis(), TimeUnit.MILLISECONDS)
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
		return 8081;
	}
}
