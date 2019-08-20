package com.hiddenswitch.spellsource.concurrent.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.SuspendableCondition;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.Sync.invoke0;
import static io.vertx.ext.sync.Sync.awaitResult;

public final class SuspendableEventBusCondition implements SuspendableCondition, AutoCloseable, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SuspendableEventBusCondition.class);
	private static final String NOT_EMPTY = "not empty";
	private static final String DESTROYED = "destroyed";
	public static final long SEND_TIMEOUT = 1000L;
	private final String address;

	public SuspendableEventBusCondition(String name) {
		this.address = "SuspendableEventBusCondition/consumer/" + name;
	}

	@Override
	@Suspendable
	public boolean await() {
		HandlerReceiverAdaptor<Buffer> adaptor = Sync.streamAdaptor();
		MessageConsumer<Buffer> consumer = Vertx.currentContext().owner().eventBus().consumer(address);
		Future<Void> future = Future.future();
		consumer.completionHandler(future);
		consumer.bodyStream().handler(adaptor);
		Buffer message;
		Void t = awaitResult(h -> future.setHandler(h));

		try {
			LOGGER.trace("vertx {} awaiting on {}", getNodeID(), address);
			message = adaptor.receive();
			LOGGER.trace("vertx {} received {}", getNodeID(), message);
		} catch (VertxException ex) {
			// Timed out or interrupted, doesn't really matter but it's bad.
			if (ex.getCause() instanceof TimeoutException
					|| ex.getCause() instanceof InterruptedException) {
				return false;
			}
			// Not recoverable
			throw ex;
		} finally {
			adaptor.receivePort().close();
			consumer.unregister();
		}

		if (message != null && !message.toString().equals(NOT_EMPTY)) {
			throw new AssertionError("not ok");
		}

		return true;
	}

	@Override
	@Suspendable
	public long awaitMillis(long millis) {
		long start = System.currentTimeMillis();
		HandlerReceiverAdaptor<Buffer> adaptor = Sync.streamAdaptor();
		MessageConsumer<Buffer> consumer = Vertx.currentContext().owner().eventBus().consumer(address);
		Future<Void> future = Future.future();
		consumer.completionHandler(future);
		consumer.bodyStream().handler(adaptor);
		Buffer message;
		Void t = awaitResult(h -> future.setHandler(h));
		long res = 0;

		try {
			LOGGER.trace("vertx {} awaiting on {}", getNodeID(), address);
			message = adaptor.receive(millis);
			LOGGER.trace("vertx {} received {}", getNodeID(), message);
			if (message != null && message.toString().equals(DESTROYED)) {
				return 0;
			}

			if (message != null && !message.toString().equals(NOT_EMPTY)) {
				throw new AssertionError(message);
			}
			long end = System.currentTimeMillis();
			res = Math.max(0, millis - (end - start));
		} catch (VertxException ex) {
			// Timed out or interrupted, doesn't really matter but it's bad.
			if (ex.getCause() instanceof TimeoutException
					|| ex.getCause() instanceof InterruptedException) {
			} else {
				// Not recoverable
				throw ex;
			}
		} finally {
			adaptor.receivePort().close();
			Void v2 = awaitResult(consumer::unregister);
		}
		return res;
	}

	public String getNodeID() {
		return ((VertxInternal) Vertx.currentContext().owner()).getNodeID();
	}

	@Override
	@Suspendable
	public void signalAll() {
		Vertx.currentContext()
				.owner()
				.eventBus()
				.publish(address, Buffer.buffer(NOT_EMPTY), new DeliveryOptions().setSendTimeout(SEND_TIMEOUT));
		LOGGER.trace("vertx {} signalled all to {}", getNodeID(), address);
	}

	@Override
	@Suspendable
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		completionHandler.handle(Future.succeededFuture());
	}

	@Suspendable
	public void destroy(Handler<AsyncResult<Void>> completionHandler) {
		Vertx.currentContext()
				.owner()
				.eventBus()
				.publish(address, Buffer.buffer(DESTROYED), new DeliveryOptions().setSendTimeout(SEND_TIMEOUT));
		close(completionHandler);
	}

	@Override
	@Suspendable
	public void close() throws Exception {
		close((fut) -> {
		});
	}
}
