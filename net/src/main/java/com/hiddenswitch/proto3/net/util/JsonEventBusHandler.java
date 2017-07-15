package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class JsonEventBusHandler<T, R> implements Handler<Message<JsonObject>> {
	private final SuspendableFunction<T, R> method;
	private final Class<? extends T> returnClass;

	JsonEventBusHandler(SuspendableFunction<T, R> method, Class<? extends T> returnClass) {
		this.method = method;
		this.returnClass = returnClass;
	}

	@Override
	@Suspendable
	public void handle(Message<JsonObject> message) {
		T request = Serialization.deserialize(message.body(), returnClass);
		R response = null;

		try {
			response = method.apply(request);
		} catch (InterruptedException | SuspendExecution e) {
			e.printStackTrace();
		} catch (Throwable e) {
			message.fail(-1, BufferEventBusHandler.getMessage(e));
		}

		message.reply(new JsonObject(Serialization.serialize(response)));
	}

}
