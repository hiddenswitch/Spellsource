package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.util.Serialization;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

import java.io.IOException;

class ReplyHandler implements SuspendableAction1<AsyncResult<Message<Object>>> {
	private final Handler<AsyncResult<Object>> next;

	ReplyHandler(Handler<AsyncResult<Object>> next) {
		this.next = next;
	}

	@Override
	@Suspendable
	public void call(AsyncResult<Message<Object>> reply) {
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
