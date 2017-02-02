package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by bberman on 12/7/16.
 */
public class Consumer {
	@Suspendable
	public static <T, R> Handler<Message<Buffer>> of(BiConsumer<T, Handler<AsyncResult<R>>> method) {
		return new AsyncMethodEventBusHandler<T, R>(method);
	}

	@Suspendable
	public static <T, R> SyncMethodEventBusHandler<T, R> of(Function<T, R> method) {
		// Get the context at the time of calling this function
		final Context context = Vertx.currentContext();
		return new SyncMethodEventBusHandler<>(context, method);
	}

	private static class MethodExecutedHandler<R> implements Handler<AsyncResult<R>> {
		private final Message<Buffer> message;

		public MethodExecutedHandler(Message<Buffer> message) {
			this.message = message;
		}

		@Override
		@Suspendable
		public void handle(AsyncResult<R> then) {
			if (then.succeeded()) {
				Buffer reply = Buffer.buffer(512);
				try {
					Serialization.serialize(then.result(), new VertxBufferOutputStream(reply));
				} catch (IOException e) {
					message.fail(1, e.getCause().getMessage());
					return;
				}

				message.reply(reply);
			} else {
				Throwable deepest = then.cause();
				while (deepest.getCause() != null) {
					deepest = deepest.getCause();
				}
				deepest.printStackTrace();
				message.fail(1, deepest.getMessage());
			}
		}
	}

	private static class AsyncMethodEventBusHandler<T, R> implements Handler<Message<Buffer>> {
		private final BiConsumer<T, Handler<AsyncResult<R>>> method;

		public AsyncMethodEventBusHandler(BiConsumer<T, Handler<AsyncResult<R>>> method) {
			this.method = method;
		}

		@Override
		@Suspendable
		public void handle(Message<Buffer> message) {
			VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());

			final T request;
			try {
				request = Serialization.deserialize(inputStream);
			} catch (IOException | ClassNotFoundException e) {
				message.fail(1, e.getMessage());
				return;
			}

			method.accept(request, new MethodExecutedHandler<>(message));
		}
	}

	private static class ExecuteBlockingMethodHandler<T, R> implements Handler<Future<Object>> {
		private final Message<Buffer> message;
		private final Function<T, R> method;

		public ExecuteBlockingMethodHandler(Message<Buffer> message, Function<T, R> method) {
			this.message = message;
			this.method = method;
		}

		@Override
		@Suspendable
		public void handle(Future<Object> blocking) {
			VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());
			try {
				T request = Serialization.deserialize(inputStream);
				R response = method.apply(request);
				if (response == null) {
					blocking.fail(new NullPointerException());
					return;
				}

				Buffer reply = Buffer.buffer(512);
				Serialization.serialize(response, new VertxBufferOutputStream(reply));
				blocking.complete(reply);
			} catch (Throwable e) {
				blocking.fail(e);
			}
		}
	}

	private static class BlockingMethodCompletionHandler implements Handler<AsyncResult<Object>> {
		private final Message<Buffer> message;

		public BlockingMethodCompletionHandler(Message<Buffer> message) {
			this.message = message;
		}

		@Override
		@Suspendable
		public void handle(AsyncResult<Object> then) {
			if (then.succeeded()) {
				message.reply(then.result());
			} else {
				Throwable deepest = then.cause();
				while (deepest.getCause() != null) {
					deepest = deepest.getCause();
				}
				deepest.printStackTrace();
				message.fail(1, deepest.getMessage());
			}
		}
	}

	private static class SyncMethodEventBusHandler<T, R> implements Handler<Message<Buffer>> {
		private final Context context;
		private final Function<T, R> method;

		public SyncMethodEventBusHandler(Context context, Function<T, R> method) {
			this.context = context;
			this.method = method;
		}

		@Override
		@Suspendable
		public void handle(Message<Buffer> message) {
			VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());
			T request = null;
			try {
				request = Serialization.deserialize(inputStream);
			} catch (IOException | ClassNotFoundException e) {
				message.fail(1, e.getMessage());
				return;
			}

			R response = method.apply(request);

			if (response == null) {
				message.fail(1, new NullPointerException().getMessage());
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

//			context.executeBlocking(Sync.fiberHandler(new ExecuteBlockingMethodHandler<>(message, method)), false, new BlockingMethodCompletionHandler(message));
		}
	}
}

