package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;

class SuspendableEventBusCondition implements SuspendableCondition, Closeable {
	private final Vertx vertx;
	private final String address;
	private final MessageConsumer<Buffer> consumer;
	private final MessageProducer<Buffer> producer;
	private final HandlerReceiverAdaptor<Message<Buffer>> adaptor;

	SuspendableEventBusCondition(String name) {
		this.vertx = Vertx.currentContext().owner();
		this.address = "SuspendableEventBusCondition::consumer-" + name;
		this.consumer = vertx.eventBus().consumer(address);
		this.producer = vertx.eventBus().sender(address);
		this.adaptor = Sync.streamAdaptor();
		consumer.handler(adaptor);
	}

	@Override
	@Suspendable
	public long awaitMillis(long millis) {
		long start = System.currentTimeMillis();
		Message<Buffer> res = adaptor.receive(millis);
		long end = System.currentTimeMillis();
		return millis - (end - start);
	}

	@Override
	@Suspendable
	public void signal() {
		producer.send(Buffer.buffer("ok"));
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		adaptor.receivePort().close();
		producer.close();
		consumer.unregister(completionHandler);
	}
}
