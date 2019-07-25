package com.hiddenswitch.spellsource.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.hiddenswitch.spellsource.Connection;
import com.hiddenswitch.spellsource.Tracing;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.MessageType;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.streams.WriteStream;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

public class ConnectionImpl implements Connection {
	private ServerWebSocket socket;
	private SpanContext parentSpan;
	private final String userId;
	private final List<Handler<Throwable>> exceptionHandlers = new ArrayList<>();
	private final List<Handler<Void>> drainHandlers = new ArrayList<>();
	private final List<Handler<Envelope>> handlers = new ArrayList<>();
	private final List<Handler<Void>> endHandlers = new ArrayList<>();
	private final String eventBusAddress;

	public ConnectionImpl(String userId, String eventBusAddress) {
		this.userId = userId;
		this.eventBusAddress = eventBusAddress;
	}

	@Override
	public void setSocket(ServerWebSocket socket, Handler<AsyncResult<Void>> readyHandler, SpanContext parentSpan) {
		this.socket = socket;
		this.parentSpan = parentSpan;
		String eventBusAddress = getEventBusAddress();
		EventBus eventBus = Vertx.currentContext().owner().eventBus();
		MessageConsumer<Envelope> consumer = eventBus.consumer(eventBusAddress);
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

		MessageConsumer<Buffer> closeConsumer = eventBus.consumer(getEventBusCloserAddress());
		closeConsumer.handler(msg -> {
			try {
				socket.close();
				span.finish();
				msg.reply(Buffer.buffer("closed"));
			} catch (IllegalStateException alreadyClosed) {
				msg.reply(Buffer.buffer("already closed"));
			} catch (Throwable any) {
				msg.fail(-1, "could not close");
			}
		});

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

		socket.endHandler(suspendableHandler(v1 -> {
			try {
				span.log("ending");
				for (Handler<Void> handler : endHandlers) {
					handler.handle(v1);
				}
				exceptionHandlers.clear();
				drainHandlers.clear();
				handlers.clear();
				endHandlers.clear();
				consumer.unregister();
				closeConsumer.unregister();
				span.log("ended");
			} catch (Throwable any) {
				Tracing.error(any, span, true);
			} finally {
				span.finish();
			}
		}));

		if (readyHandler != null) {
			Future<Void> v1 = Future.future();
			Future<Void> v2 = Future.future();
			consumer.completionHandler(v1);
			closeConsumer.completionHandler(v2);
			CompositeFuture.join(v1, v2).setHandler(v -> {
				readyHandler.handle(v.succeeded() ? Future.succeededFuture() : Future.failedFuture(v.cause()));
			});
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
	public Connection endHandler(Handler<Void> endHandler) {
		endHandlers.add(endHandler);
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
		end(completionHandler);
	}

	public String getEventBusAddress() {
		return eventBusAddress;
	}

	public String getEventBusCloserAddress() {
		return eventBusAddress + "/closer";
	}
}
