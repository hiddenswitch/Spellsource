package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutRequest;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpc;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public class Matchmaking extends AbstractVerticle {

	public static Future<ServerServiceDefinition> services() {
		return Future.succeededFuture(new VertxMatchmakingGrpc.MatchmakingVertxImplBase() {
			@Override
			public void enqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response) {
				super.enqueue(request, response);
			}
		}).compose(Accounts::requiresAuthorization);
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {

	}
}
