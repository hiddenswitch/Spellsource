package com.hiddenswitch.framework;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class Gateway extends AbstractVerticle {
	VertxServer server;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		var builder = VertxServerBuilder.forPort(vertx, grpcPort());

		// TODO: Just add all the services here
		CompositeFuture.join(
				Legacy.services(),
				Matchmaking.services(),
				Accounts.unauthenticatedService(),
				Accounts.authenticatedService())
				.compose(services -> {
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
					return Future.succeededFuture();
				})
				.compose(ignored -> {
					var promise = Promise.<Void>promise();
					server = builder.build();
					server.start(promise);
					return promise.future();
				})
				.onComplete(startPromise);
	}

	public static int grpcPort() {
		return 8081;
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		if (server != null && !server.isTerminated()) {
			server.shutdown(stopPromise);
		} else {
			stopPromise.complete();
		}
	}
}
