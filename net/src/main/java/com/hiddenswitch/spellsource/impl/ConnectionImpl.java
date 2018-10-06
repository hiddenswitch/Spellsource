package com.hiddenswitch.spellsource.impl;

import com.github.fromage.quasi.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.Connection;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

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
	private String id;

	public ConnectionImpl(String userId) {
		this.userId = userId;

	}

	@Override
	public void setSocket(ServerWebSocket socket) {
		this.socket = socket;

		MessageConsumer<Envelope> consumer = Vertx.currentContext().owner().eventBus().consumer(id);
		consumer.handler(msg -> {
			socket.write(Buffer.buffer(Json.encode(msg.body())));
		});

		socket.handler(buf -> {
			Envelope decoded = Json.decodeValue(buf, Envelope.class);
			for (Handler<Envelope> handler : handlers) {
				handler.handle(decoded);
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

		socket.endHandler(suspendableHandler((SuspendableAction1<Void>) v -> {
			for (Handler<Void> handler : endHandlers) {
				handler.handle(v);
			}
			exceptionHandlers.clear();
			drainHandlers.clear();
			handlers.clear();
			endHandlers.clear();
			consumer.unregister();
			Connection.getConnections().remove(new UserId(userId));
		}));
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
		socket.end();
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
	public String userId() {
		return userId;
	}

	@Override
	public Connection removeHandler(Handler<JsonObject> handler) {
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

	public ConnectionImpl setId(String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}
}
