package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.ConnectionImpl;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public interface Connection extends ReadStream<JsonObject>, WriteStream<JsonObject>, Closeable {

	static Connection create(ServerWebSocket socket, String userId) {
		return new ConnectionImpl(socket, userId);
	}

	@Override
	default Connection exceptionHandler(Handler<Throwable> handler) {
		return null;
	}

	@Override
	default Connection write(JsonObject data) {
		return null;
	}

	@Override
	default void end() {
	}

	@Override
	default Connection setWriteQueueMaxSize(int maxSize) {
		return null;
	}

	@Override
	default boolean writeQueueFull() {
		return false;
	}

	@Override
	default Connection drainHandler(Handler<Void> handler) {
		return null;
	}

	@Override
	default Connection handler(Handler<JsonObject> handler) {
		return null;
	}

	@Override
	default Connection pause() {
		return null;
	}

	@Override
	default Connection resume() {
		return null;
	}

	@Override
	default Connection endHandler(Handler<Void> endHandler) {
		return null;
	}

	String userId();
}
