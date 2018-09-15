package com.hiddenswitch.spellsource.util;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.concurrent.SuspendableFunction;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

class JsonEventBusHandler<T, R> implements SuspendableAction1<Message<String>> {
	private final SuspendableFunction<T, R> method;
	private final Class<? extends T> requestClass;

	JsonEventBusHandler(SuspendableFunction<T, R> method, Class<? extends T> requestClass) {
		this.method = method;
		this.requestClass = requestClass;
	}

	@Override
	@Suspendable
	public void call(Message<String> message) {
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
