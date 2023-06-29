package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.rpc.VertxUnauthenticatedGrpcServer;
import io.vertx.grpc.server.GrpcServer;

@FunctionalInterface
public interface BindAll<T> {
	T bindAll(GrpcServer server);

	default void bind(GrpcServer server) {
		bindAll(server);
	}
}
