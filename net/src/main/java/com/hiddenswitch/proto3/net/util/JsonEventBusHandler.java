package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

class JsonEventBusHandler<T, R> implements Handler<Message<String>> {
	private final SuspendableFunction<T, R> method;
	private final Class<? extends T> requestClass;

	JsonEventBusHandler(SuspendableFunction<T, R> method, Class<? extends T> requestClass) {
		this.method = method;
		this.requestClass = requestClass;
	}

	@Override
	@Suspendable
	public void handle(Message<String> message) {
		T request = Serialization.deserialize(message.body(), requestClass);
		R response = null;

		try {
			response = method.apply(request);
		} catch (InterruptedException | SuspendExecution e) {
			e.printStackTrace();
		} catch (Throwable e) {
			message.fail(-1, BufferEventBusHandler.getMessage(e));
		}

		message.reply(Serialization.serialize(response));
	}

}
