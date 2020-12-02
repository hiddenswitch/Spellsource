package com.hiddenswitch.framework;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
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
				Matchmaking.services(),
				Accounts.unauthenticatedService(),
				Accounts.authenticatedService())
				.compose(services -> {
					var serverConfiguration = Environment.cachedConfigurationOrGet();
					var builder = VertxServerBuilder.forPort(vertx, grpcPort());

					var list = services.list();
					for (var service : list) {
						if (service instanceof BindableService) {
							builder.addService((BindableService) service);
						} else if (service instanceof ServerServiceDefinition) {
							builder.addService((ServerServiceDefinition) service);
						} else {
							return Future.failedFuture("invalid service");
						}
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

//
//	@Override
//	public void stop(Promise<Void> stopPromise) throws Exception {
//		if (server != null && !server.isTerminated()) {
//			server.shutdown(stopPromise);
//		} else {
//			stopPromise.complete();
//		}
//	}
}
