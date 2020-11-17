package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.ext.sync.concurrent.SuspendableFunction;
import com.hiddenswitch.spellsource.util.Serialization;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.OptionalDataException;
import java.lang.reflect.InvocationTargetException;

/**
 * Uses buffers to transmit data across the event bus for calls.
 */
class BufferEventBusHandler<T, R> implements SuspendableAction1<Message<Buffer>> {
	private final SuspendableFunction<T, R> method;

	BufferEventBusHandler(SuspendableFunction<T, R> method) {
		this.method = method;
	}

	@Override
	@Suspendable
	public void call(Message<Buffer> message) {
		VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());
		T request = null;
		try {
			request = Serialization.deserialize(inputStream);
		} catch (OptionalDataException invalidMessageError) {
			message.fail(1, "An invalid data exception occurred. This was the buffer:\n" + message.body().toString());
			return;
		} catch (IOException | ClassNotFoundException e) {
			message.fail(1, e.getMessage());
			return;

		}

		R response = null;

		try {
			response = method.apply(request);
		} catch (InterruptedException | SuspendExecution e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			message.fail(-1, getMessage(e.getCause()));
			return;
		} catch (Throwable e) {
			message.fail(-1, getMessage(e));
			return;
		}

		Buffer reply = Buffer.buffer(512);

		try {
			Serialization.serialize(response, new VertxBufferOutputStream(reply));
		} catch (IOException e) {
			message.fail(1, e.getMessage());
			return;
		}

		message.reply(reply);
	}

	static String getMessage(Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append("\nMessage Reply Error\n");
		sb.append(ExceptionUtils.getMessage(e));
		sb.append("\nStart API Stack Trace\n");
		sb.append(ExceptionUtils.getStackTrace(e));
		sb.append("\nEnd API Stack Trace\nStart Caller Error");
		return sb.toString();
	}
}