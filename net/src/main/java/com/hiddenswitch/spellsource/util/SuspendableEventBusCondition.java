package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;

class SuspendableEventBusCondition implements SuspendableCondition, Closeable {
	private final String address;
	private final MessageProducer<Buffer> producer;

	SuspendableEventBusCondition(String name) {
		this.address = "SuspendableEventBusCondition::consumer-" + name;
		this.producer = Vertx.currentContext().owner().eventBus().sender(address);
	}

	@Override
	@Suspendable
	public long awaitMillis(long millis) {
		long start = System.currentTimeMillis();
		HandlerReceiverAdaptor<Buffer> adaptor = Sync.streamAdaptor();
		MessageConsumer<Buffer> consumer = Vertx.currentContext().owner().eventBus().consumer(address);
		consumer.bodyStream().handler(adaptor);
		Buffer res = adaptor.receive(millis);
		if (res != null && !res.toString().equals("ok")) {
			throw new AssertionError("not ok");
		}
		long end = System.currentTimeMillis();
		adaptor.receivePort().close();
		consumer.unregister();
		return millis - (end - start);
	}

	@Override
	@Suspendable
	public void signal() {
		producer.send(Buffer.buffer("ok"));
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		producer.close();
		completionHandler.handle(null);
	}
}
