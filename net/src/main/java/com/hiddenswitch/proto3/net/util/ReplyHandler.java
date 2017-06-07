package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import java.io.IOException;

class ReplyHandler implements Handler<AsyncResult<Message<Object>>> {
	private final Handler<AsyncResult<Object>> next;

	ReplyHandler(Handler<AsyncResult<Object>> next) {
		this.next = next;
	}

	@Override
	@Suspendable
	public void handle(AsyncResult<Message<Object>> reply) {
		if (reply.succeeded()) {
			try {
				Object body = Serialization.deserialize(new VertxBufferInputStream((Buffer) reply.result().body()));
				next.handle(Future.succeededFuture(body));
			} catch (IOException | ClassNotFoundException e) {
				next.handle(Future.failedFuture(e));
			}
		} else {
			next.handle(Future.failedFuture(reply.cause()));
		}
	}
}
