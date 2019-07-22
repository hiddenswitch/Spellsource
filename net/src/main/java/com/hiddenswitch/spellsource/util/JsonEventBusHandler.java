package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.google.common.base.Throwables;
import com.hiddenswitch.spellsource.concurrent.SuspendableFunction;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import javax.management.ReflectionException;
import java.lang.reflect.InvocationTargetException;

class JsonEventBusHandler<T, R> implements SuspendableAction1<Message<JsonObject>> {
	private final SuspendableFunction<T, R> method;
	private final Class<? extends T> requestClass;

	JsonEventBusHandler(SuspendableFunction<T, R> method, Class<? extends T> requestClass) {
		this.method = method;
		this.requestClass = requestClass;
	}

	@Override
	@Suspendable
	public void call(Message<JsonObject> message) {
		T request = message.body().mapTo(requestClass);
		R response = null;

		try {
			response = method.apply(request);
		} catch (InterruptedException | SuspendExecution | IllegalAccessException exception) {
			message.fail(500, BufferEventBusHandler.getMessage(exception));
			throw new RuntimeException(exception);
		} catch (RuntimeException | InvocationTargetException runtimeException) {
			Throwable rootCause = Throwables.getRootCause(runtimeException);
			message.fail(400, BufferEventBusHandler.getMessage(rootCause));
			return;
		}

		message.reply(JsonObject.mapFrom(response));
	}

}
