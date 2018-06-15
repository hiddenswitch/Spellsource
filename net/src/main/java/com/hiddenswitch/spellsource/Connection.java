package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.impl.ConnectionImpl;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.SharedData;
import com.hiddenswitch.spellsource.util.SuspendableMap;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface Connection extends ReadStream<Envelope>, WriteStream<Envelope>, Closeable {
	Logger logger = LoggerFactory.getLogger(SharedData.class);

	static SuspendableMap<UserId, String> getConnections() throws SuspendExecution {
		return SuspendableMap.getOrCreate("Connection::connections");
	}

	static void getConnections(Handler<AsyncResult<AsyncMap<UserId, String>>> handler) {
		SuspendableMap.getOrCreate("Connection::connections", handler);
	}

	/**
	 * Retrieves a valid reference to write to a connection from anywhere, as long as the event bus on the other node is
	 * shared/clustered with this one.
	 *
	 * @param userId The user ID whose connection should be retrieved
	 * @return A connection object.
	 */
	static WriteStream<Envelope> writeStream(String userId) throws SuspendExecution {
		SuspendableMap<UserId, String> connections = getConnections();
		String handlerId = connections.get(new UserId(userId));
		if (handlerId == null) {
			return null;
		}

		MessageProducer<Buffer> producer = Vertx.currentContext().owner().eventBus().publisher(handlerId);

		return new WriteStream<Envelope>() {
			@Override
			public WriteStream<Envelope> exceptionHandler(Handler<Throwable> handler) {
				producer.exceptionHandler(handler);
				return this;
			}

			@Override
			public WriteStream<Envelope> write(Envelope data) {
				producer.write(Json.encodeToBuffer(data));
				return this;
			}

			@Override
			public void end() {
				producer.end();
			}

			@Override
			public WriteStream<Envelope> setWriteQueueMaxSize(int maxSize) {
				producer.setWriteQueueMaxSize(maxSize);
				return this;
			}

			@Override
			public boolean writeQueueFull() {
				return producer.writeQueueFull();
			}

			@Override
			public WriteStream<Envelope> drainHandler(Handler<Void> handler) {
				producer.drainHandler(handler);
				return this;
			}
		};
	}

	static WriteStream<Envelope> writeStream(UserId userId) throws SuspendExecution {
		return writeStream(userId.toString());
	}

	static void writeStream(String userId, Handler<AsyncResult<WriteStream<Envelope>>> handler) {
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
		String id = "Connection::clusteredConsumer[" + socket.binaryHandlerID() + "]";

		MessageConsumer<Envelope> consumer = Vertx.currentContext().owner().eventBus().consumer(id);
		consumer.handler(msg -> {
			socket.write(Json.encodeToBuffer(msg.body()));
		});

		connections.put(key, id);
		connection.endHandler(Sync.suspendableHandler(v -> {
			connections.remove(key, id);
			consumer.unregister();
		}));

		return connection;
	}

	static Handler<RoutingContext> create() {
		return Sync.suspendableHandler(Connection::connected);
	}

	static void connected(Handler<Connection> handler) {
		getHandlers().add(handler);
	}

	static Deque<Handler<Connection>> getHandlers() {
		Vertx vertx = Vertx.currentContext().owner();
		final Context context = vertx.getOrCreateContext();
		Deque<Handler<Connection>> handlers = context.get("Connection::handlers");

		if (handlers == null) {
			handlers = new ConcurrentLinkedDeque<>();
			context.put("Connection::handlers", handlers);
		}
		return handlers;
	}

	@Suspendable
	static Lock methodLock(String userId) {
		return SharedData.lock("Connection::method-ordering-lock[" + userId + "]");
	}

	@Suspendable
	static void connected(RoutingContext routingContext) throws SuspendExecution {
		registerCodecs();

		String userId = Accounts.userId(routingContext);
		Lock lock;

		try {
			lock = SharedData.lock("Connection::realtime-data-lock[" + userId + "]", 1000L);
		} catch (VertxException ex) {
			routingContext.fail(ex);
			return;
		}

		ServerWebSocket socket = routingContext.request().upgrade();
		Deque<Handler<Connection>> handlers = getHandlers();
		Connection connection = create(socket, userId);

		// All handlers should run simultaneously
		for (Handler<Connection> handler : handlers) {
			Vertx.currentContext().runOnContext(v -> {
				try {
					handler.handle(connection);
				} catch (Throwable t) {
					logger.error("connected {} {}: Handler threw an exception, propagated error", userId, routingContext.request().connection().remoteAddress());
					throw t;
				}
			});
		}

		// The lock gets released when the user disconnects
		connection.endHandler(v -> lock.release());
	}

	static void registerCodecs() {
		try {
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(Envelope.class, new EnvelopeMessageCodec());
		} catch (IllegalStateException alreadyRegistered) {
			// Ignored
		}
	}

	@Override
	default Connection exceptionHandler(Handler<Throwable> handler) {
		return null;
	}

	@Override
	default Connection write(Envelope data) {
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
	default Connection handler(Handler<Envelope> handler) {
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

	class EnvelopeMessageCodec implements MessageCodec<Envelope, Envelope> {

		@Override
		public void encodeToWire(Buffer buffer, Envelope envelope) {
			JsonObject.mapFrom(envelope).writeToBuffer(buffer);
		}

		@Override
		public Envelope decodeFromWire(int pos, Buffer buffer) {
			JsonObject obj = new JsonObject();
			obj.readFromBuffer(pos, buffer);
			return obj.mapTo(Envelope.class);
		}

		@Override
		public Envelope transform(Envelope envelope) {
			return envelope;
		}

		@Override
		public String name() {
			return "envelope";
		}

		@Override
		public byte systemCodecID() {
			return -1;
		}
	}
}
