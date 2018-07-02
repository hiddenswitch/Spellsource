package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.impl.ConnectionImpl;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.Hazelcast;
import com.hiddenswitch.spellsource.concurrent.SuspendableLock;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
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
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

public interface Connection extends ReadStream<Envelope>, WriteStream<Envelope>, Closeable {
	Logger logger = LoggerFactory.getLogger(Hazelcast.class);

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

		MessageProducer<Envelope> producer = Vertx.currentContext().owner().eventBus().publisher(handlerId);
		return producer;
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

	static Connection create(String userId) throws SuspendExecution {
		SuspendableMap<UserId, String> connections = getConnections();
		String id = "Connection::clusteredConsumer[" + userId + "]";
		connections.put(new UserId(userId), id);
		return new ConnectionImpl(userId).setId(id);
	}

	static Handler<RoutingContext> handler() {
		return suspendableHandler((SuspendableAction1<RoutingContext>) Connection::connected);
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
	static SuspendableLock methodLock(String userId) {
		return SuspendableLock.lock("Connection::method-ordering-lock[" + userId + "]");
	}

	@Suspendable
	static void connected(RoutingContext routingContext) throws SuspendExecution {
		if (routingContext.failed()) {
			routingContext.next();
			return;
		}

		registerCodecs();

		String userId = Accounts.userId(routingContext);

		if (userId == null) {
			routingContext.fail(403);
			routingContext.next();
			return;
		}

		SuspendableLock lock = SuspendableLock.lock("Connection::realtime[" + userId + "]");

		Deque<Handler<Connection>> handlers = getHandlers();
		Connection connection = create(userId);

		// All handlers should run simultaneously
		for (Handler<Connection> handler : handlers) {
			Vertx.currentContext().runOnContext(v -> handler.handle(connection));
		}

		// The lock gets released when the user disconnects
		connection.endHandler(v -> lock.release());

		ServerWebSocket socket = routingContext.request().upgrade();
		connection.setSocket(socket);
	}

	static void registerCodecs() {
		try {
			Vertx.currentContext().owner().eventBus().registerDefaultCodec(Envelope.class, new EnvelopeMessageCodec());
		} catch (IllegalStateException alreadyRegistered) {
			// Ignored
		}
	}

	void setSocket(ServerWebSocket socket);

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
