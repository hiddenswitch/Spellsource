package com.hiddenswitch.spellsource.net.impl;

import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.core.JsonConfiguration;
import com.hiddenswitch.spellsource.net.Connection;
import com.hiddenswitch.spellsource.common.Tracing;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.ext.sync.Sync.awaitEvent;
import static io.vertx.ext.sync.Sync.awaitResult;

public class ConnectionImpl implements Connection {
	static {
		JsonConfiguration.configureJson();
	}

	private ServerWebSocket socket;
	private final String userId;
	private final List<Handler<Throwable>> exceptionHandlers = new ArrayList<>();
	private final List<Handler<Void>> drainHandlers = new ArrayList<>();
	private final List<Handler<Envelope>> handlers = new ArrayList<>();
	private final List<Handler<Promise<Void>>> closeHandlers = new ArrayList<>();
	private final String eventBusAddress;
	private final AtomicBoolean closing = new AtomicBoolean(false);
	private final List<Handler<AsyncResult<Void>>> awaitClosers = new CopyOnWriteArrayList<>();
	private MessageConsumer<Envelope> consumer;
	private CompositeFuture closedFuture;

	public ConnectionImpl(String userId, String eventBusAddress) {
		this.userId = userId;
		this.eventBusAddress = eventBusAddress;
	}

	@Override
	public void setSocket(ServerWebSocket socket, Handler<AsyncResult<Void>> readyHandler, SpanContext parentSpan) {
		if (this.socket != null) {
			throw new UnsupportedOperationException();
		}
		this.socket = socket;
		String eventBusAddress = getEventBusAddress();
		EventBus eventBus = Vertx.currentContext().owner().eventBus();
		consumer = eventBus.consumer(eventBusAddress);
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Connection/internal")
				.asChildOf(parentSpan)
				.withTag("userId", userId)
				.withTag(Tags.PEER_HOSTNAME, socket.remoteAddress().host())
				.start();

		// Write to the socket when we receive a message on the event bus
		consumer.handler(msg -> socket.write(Json.encodeToBuffer(msg.body()), written -> {
			if (!written.succeeded()) {
				msg.fail(-1, written.cause().getMessage());
			}
		}));

		// Read handler
		socket.handler(buf -> {
			Envelope decoded = Json.decodeValue(buf, Envelope.class);
			span.log(ImmutableMap.of(Fields.EVENT, "received", "size", buf.length()));
			for (Handler<Envelope> handler : handlers) {
				try {
					handler.handle(decoded);
				} catch (RuntimeException runtimeException) {
					Tracing.error(runtimeException, span, false);
				}
			}
		});

		// Logging of exceptions is handled in caller
		socket.exceptionHandler(t -> {
			for (Handler<Throwable> handler : exceptionHandlers) {
				handler.handle(t);
			}
		});

		socket.drainHandler(v -> {
			span.log("drained");
			for (Handler<Void> handler : drainHandlers) {
				handler.handle(v);
			}
		});

		// Client closed the connection
		socket.endHandler(v1 -> handleClose(Promise.promise()));

		if (readyHandler != null) {
			consumer.completionHandler(readyHandler);
		}
	}

	private void handleClose(Handler<AsyncResult<Void>> completed) {
		if (closing.compareAndSet(false, true)) {
			var promises = new ArrayList<Promise<Void>>();
			for (Handler<Promise<Void>> handler : closeHandlers) {
				var promise = Promise.<Void>promise();
				promises.add(promise);
				handler.handle(promise);
			}
			exceptionHandlers.clear();
			drainHandlers.clear();
			handlers.clear();
			closeHandlers.clear();
			var promise = Promise.<Void>promise();
			promises.add(promise);
			consumer.unregister(promise);
			closedFuture = CompositeFuture.all(promises.stream().map(Promise::future).collect(Collectors.toList()));
			closedFuture.onComplete(v -> {
				completed.handle(v.mapEmpty());
				for (var closer : awaitClosers) {
					closer.handle(v.mapEmpty());
				}
			});
		} else {
			if (closedFuture != null) {
				for (var i = 0; i < closedFuture.size(); i++) {
					if (!closedFuture.isComplete(i)) {
						awaitClosers.add(completed);
						return;
					}
				}
			}
			completed.handle(Future.succeededFuture());
		}
	}

	@Override
	public Connection exceptionHandler(Handler<Throwable> handler) {
		exceptionHandlers.add(handler);
		return this;
	}

	@Override
	public Connection write(@NotNull Envelope data) {
		socket.write(Json.encodeToBuffer(data));
		return this;
	}

	@Override
	public Connection write(Envelope data, Handler<AsyncResult<Void>> handler) {
		socket.write(Json.encodeToBuffer(data), handler);
		return this;
	}

	@Override
	public void end() {
		if (socket != null) {
			socket.end();
		}
	}

	@Override
	public void end(Handler<AsyncResult<Void>> handler) {
		if (socket != null) {
			socket.end(handler);
		}
	}

	@Override
	public Connection setWriteQueueMaxSize(int maxSize) {
		socket.setWriteQueueMaxSize(maxSize);
		return this;
	}

	@Override
	public boolean writeQueueFull() {
		return socket.writeQueueFull();
	}

	@Override
	public Connection drainHandler(Handler<Void> handler) {
		drainHandlers.add(handler);
		return this;
	}

	@Override
	public Connection handler(Handler<Envelope> handler) {
		handlers.add(handler);
		return this;
	}

	@Override
	public Connection pause() {
		socket.pause();
		return this;
	}

	@Override
	public Connection resume() {
		socket.resume();
		return this;
	}

	@Override
	public Connection fetch(long amount) {
		socket.resume();
		socket.fetch(amount);
		return this;
	}

	@Override
	public Connection addCloseHandler(Handler<Promise<Void>> closeHandler) {
		closeHandlers.add(closeHandler);
		return this;
	}

	@Override
	@NotNull
	public String userId() {
		return userId;
	}

	@Override
	public Connection removeHandler(Handler<Envelope> handler) {
		handlers.remove(handler);
		return this;
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		handleClose(v -> end(completionHandler));
	}

	public String getEventBusAddress() {
		return eventBusAddress;
	}
}
