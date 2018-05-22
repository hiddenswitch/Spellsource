package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.impl.ConnectionImpl;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.SharedData;
import com.hiddenswitch.spellsource.util.SuspendableMap;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public interface Connection extends ReadStream<JsonObject>, WriteStream<JsonObject>, Closeable {
	static SuspendableMap<UserId, String> getConnections() throws SuspendExecution {
		return SharedData.getClusterWideMap("Connection::connections");
	}

	static void getConnections(Handler<AsyncResult<AsyncMap<UserId, String>>> handler) {
		SharedData.getClusterWideMap("Connection::connections", handler);
	}

	/**
	 * Retrieves a valid reference to a connection from anywhere, as long as the event bus on the other node is shared/
	 * clustered with this one.
	 *
	 * @param userId The user ID whose connection should be retrieved
	 * @return A connection object.
	 */
	static WriteStream<Buffer> get(String userId) throws SuspendExecution {
		SuspendableMap<UserId, String> connections = getConnections();
		String handlerId = connections.get(new UserId(userId));
		if (handlerId == null) {
			return null;
		}

		return Vertx.currentContext().owner().eventBus().publisher(handlerId);
	}

	static void get(String userId, Handler<AsyncResult<WriteStream<Buffer>>> handler) {
		getConnections(r1 -> {
			if (r1.failed()) {
				handler.handle(Future.failedFuture(r1.cause()));
			} else {
				r1.result().get(new UserId(userId), r2 -> {
					if (r2.failed()) {
						handler.handle(Future.failedFuture(r2.cause()));
					} else {
						String handlerId = r2.result();
						if (handlerId != null) {
							handler.handle(Future.succeededFuture(Vertx.currentContext().owner().eventBus().publisher(handlerId)));
						} else {
							handler.handle(Future.succeededFuture());
						}
					}
				});
			}
		});
	}

	static Connection create(ServerWebSocket socket, String userId) throws SuspendExecution {
		final ConnectionImpl connection = new ConnectionImpl(socket, userId);
		final SuspendableMap<UserId, String> connections = getConnections();
		final UserId key = new UserId(userId);
		String id = socket.binaryHandlerID();
		connections.put(key, id);
		connection.endHandler(Sync.suspendableHandler(v -> connections.remove(key, id)));
		return connection;
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

	Connection removeHandler(Handler<JsonObject> handler);
}
