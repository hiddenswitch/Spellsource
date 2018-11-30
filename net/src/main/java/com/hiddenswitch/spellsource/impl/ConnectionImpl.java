package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Connection;
import com.hiddenswitch.spellsource.client.models.Envelope;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

public class ConnectionImpl implements Connection {
	private ServerWebSocket socket;
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
	public void setSocket(ServerWebSocket socket, Handler<AsyncResult<Void>> readyHandler) {
		this.socket = socket;
		String userId = this.userId;
		String eventBusAddress = getEventBusAddress();
		MessageConsumer<Envelope> consumer = Vertx.currentContext().owner().eventBus().consumer(eventBusAddress);
		consumer.handler(msg -> {
			socket.write(Buffer.buffer(Json.encode(msg.body())));
		});

		socket.handler(buf -> {
			Envelope decoded = Json.decodeValue(buf, Envelope.class);
			for (Handler<Envelope> handler : handlers) {
				try {
					handler.handle(decoded);
				} catch (Throwable any) {
					LOGGER.error("socket handler " + userId, any);
				}
			}
		});

		socket.exceptionHandler(t -> {
			for (Handler<Throwable> handler : exceptionHandlers) {
				handler.handle(t);
			}
		});

		socket.drainHandler(v -> {
			for (Handler<Void> handler : drainHandlers) {
				handler.handle(v);
			}
		});

		socket.endHandler(suspendableHandler(v1 -> {
			Connection.LOGGER.debug("connection endHandler {}: Closing", userId);
			for (Handler<Void> handler : endHandlers) {
				handler.handle(v1);
			}
			exceptionHandlers.clear();
			drainHandlers.clear();
			handlers.clear();
			endHandlers.clear();
			consumer.unregister();
			Connection.LOGGER.debug("connection endHandler {}: Close complete, removing handler from event bus", userId);
			Connection.getConnections(connections -> {
				if (connections.failed()) {
					Connection.LOGGER.error("connection endHandler {}: Failed to unregister connection: {}", userId, connections.cause());
					return;
				}

				connections.result().removeIfPresent(new UserId(userId), eventBusAddress, v2 -> {
					Connection.LOGGER.debug("connection endHandler {}: Connection closed: {}", userId, v2.result());
				});
			});
		}));

		if (readyHandler != null) {
			consumer.completionHandler(readyHandler);
		}
	}

	@Override
	public Connection exceptionHandler(Handler<Throwable> handler) {
		exceptionHandlers.add(handler);
		return this;
	}

	@Override
	public Connection write(Envelope data) {
		socket.write(Buffer.buffer(Json.encode(data)));
		return this;
	}

	@Override
	public void end() {
		if (socket != null) {
			socket.end();
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
		try {
			end();
			completionHandler.handle(Future.succeededFuture());
		} catch (Throwable t) {
			completionHandler.handle(Future.failedFuture(t));
		}
	}

	public String getEventBusAddress() {
		return eventBusAddress;
	}
}
