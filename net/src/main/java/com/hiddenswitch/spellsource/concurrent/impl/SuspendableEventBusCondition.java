package com.hiddenswitch.spellsource.concurrent.impl;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.SuspendableCondition;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SuspendableEventBusCondition implements SuspendableCondition, Closeable {
	private final String address;
	private final MessageProducer<Buffer> producer;

	public SuspendableEventBusCondition(String name) {
		this.address = "SuspendableEventBusCondition::consumer-" + name;
		this.producer = Vertx.currentContext().owner()
				.eventBus()
				.sender(address);
		this.producer.deliveryOptions(new DeliveryOptions().setSendTimeout(1000L));
	}

	@Override
	@Suspendable
	public boolean await() {
		HandlerReceiverAdaptor<Buffer> adaptor = Sync.streamAdaptor();
		MessageConsumer<Buffer> consumer = Vertx.currentContext().owner().eventBus().consumer(address);
		consumer.bodyStream().handler(adaptor);
		Buffer res;

		try {
			res = adaptor.receive();
		} catch (VertxException ex) {
			// Timed out or interrupted, doesn't really matter but it's bad.
			if (ex.getCause() instanceof TimeoutException
					|| ex.getCause() instanceof InterruptedException) {
				return false;
			}
			// Not recoverable
			throw ex;
		}

		if (res != null && !res.toString().equals("ok")) {
			throw new AssertionError("not ok");
		}

		adaptor.receivePort().close();
		consumer.unregister();
		return true;
	}

	@Override
	@Suspendable
	public long awaitMillis(long millis) {
		long start = System.currentTimeMillis();
		HandlerReceiverAdaptor<Buffer> adaptor = Sync.streamAdaptor();
		MessageConsumer<Buffer> consumer = Vertx.currentContext().owner().eventBus().consumer(address);
		consumer.bodyStream().handler(adaptor);
		Buffer res;

		try {
			res = adaptor.receive(millis);
		} catch (VertxException ex) {
			// Timed out or interrupted, doesn't really matter but it's bad.
			if (ex.getCause() instanceof TimeoutException
					|| ex.getCause() instanceof InterruptedException) {
				return 0;
			}
			// Not recoverable
			throw ex;
		}

		if (res != null && !res.toString().equals("ok")) {
			throw new AssertionError("not ok");
		}
		long end = System.currentTimeMillis();
		adaptor.receivePort().close();
		consumer.unregister();
		return millis - (end - start);
	}

	@Override
	public void awaitMillis(long millis, Handler<AsyncResult<Void>> handler) {
		Vertx vertx = Vertx.currentContext().owner();
		AtomicBoolean succeeded = new AtomicBoolean();
		MessageConsumer<Buffer> consumer = vertx.eventBus().consumer(address);

		consumer.handler(msg -> {
			consumer.unregister();
			if (msg.body().toString().equals("ok") && succeeded.compareAndSet(false, true)) {
				handler.handle(Future.succeededFuture());
			}
		});

		vertx.setTimer(millis, timerId -> {
			consumer.unregister();
			if (!succeeded.get()) {
				handler.handle(Future.failedFuture(new TimeoutException()));
			}
		});
	}

	@Override
	@Suspendable
	public void signal() {
		producer.send(Buffer.buffer("ok"));
	}

	@Override
	@Suspendable
	public void signalAll() {
		Vertx.currentContext()
				.owner()
				.eventBus()
				.publish(address, Buffer.buffer("ok"), new DeliveryOptions().setSendTimeout(1000L));
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		producer.close();
		completionHandler.handle(null);
	}
}
