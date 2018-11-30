package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

class JsonReplyHandler implements SuspendableAction1<AsyncResult<Message<Object>>> {
	private final Handler<AsyncResult<Object>> next;
	private final Class responseClass;

	JsonReplyHandler(Handler<AsyncResult<Object>> next, Class responseClass) {
		this.next = next;
		this.responseClass = responseClass;
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public void call(AsyncResult<Message<Object>> reply) {
		if (reply.succeeded()) {
			try {
				Object body = Serialization.deserialize((String) reply.result().body(), responseClass);
				next.handle(Future.succeededFuture(body));
			} catch (RuntimeException dataException) {
				next.handle(Future.failedFuture(dataException));
			}
		} else {
			next.handle(Future.failedFuture(reply.cause()));
		}
	}
}
