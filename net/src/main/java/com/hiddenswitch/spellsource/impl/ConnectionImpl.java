package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Connection;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ConnectionImpl implements Connection {
	private final ServerWebSocket socket;
	private final String userId;
	private final List<Handler<Throwable>> exceptionHandlers = new ArrayList<>();
	private final List<Handler<Void>> drainHandlers = new ArrayList<>();
	private final List<Handler<Envelope>> handlers = new ArrayList<>();
	private final List<Handler<Void>> endHandlers = new ArrayList<>();

	public ConnectionImpl(ServerWebSocket socket, String userId) {
		this.socket = socket;
		this.userId = userId;

		socket.handler(Sync.suspendableHandler(buf -> {
			Envelope decoded = Json.decodeValue(buf, Envelope.class);
			for (Handler<Envelope> handler : handlers) {
				handler.handle(decoded);
			}
		}));

		socket.exceptionHandler(Sync.suspendableHandler(t -> {
			for (Handler<Throwable> handler : exceptionHandlers) {
				handler.handle(t);
			}
		}));

		socket.drainHandler(Sync.suspendableHandler(v -> {
			for (Handler<Void> handler : drainHandlers) {
				handler.handle(v);
			}
		}));

		socket.endHandler(Sync.suspendableHandler(v -> {
			for (Handler<Void> handler : endHandlers) {
				handler.handle(v);
			}
			exceptionHandlers.clear();
			drainHandlers.clear();
			handlers.clear();
			endHandlers.clear();
		}));
	}

	@Override
	public Connection exceptionHandler(Handler<Throwable> handler) {
		exceptionHandlers.add(handler);
		return this;
	}

	@Override
	public Connection write(Envelope data) {
		socket.write(Json.encodeToBuffer(data));
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
}
