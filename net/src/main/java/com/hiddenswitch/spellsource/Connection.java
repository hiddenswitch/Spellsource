package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.concurrent.SuspendableLock;
import com.hiddenswitch.spellsource.impl.ConnectionImpl;
import com.hiddenswitch.spellsource.impl.EnvelopeMessageCodec;
import com.hiddenswitch.spellsource.impl.UserId;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * Manages the real time data connection users get when they connect to the Spellsource server.
 * <p>
 * To set up a behaviour that uses the real time connect, use {@link Connection#connected(Handler)}, which passes you a
 * new connection to a unique user. For <b>example</b>, this code from the {@link Presence} package notifies users of
 * their presence.
 * <pre>
 *   {@code
 *     Connection.connected(Sync.suspendableHandler(connection -> {
 * 	  		final UserId key = new UserId(connection.userId());
 * 	  		connection.endHandler(Sync.suspendableHandler(ignored -> {
 * 	  			setPresence(key, PresenceEnum.OFFLINE);
 * 	  		}));
 *
 * 	  		// Once the user is connected, set their status to online
 * 	  		setPresence(key, PresenceEnum.ONLINE);
 * 	   }));
 * 	   ...
 * 	   static void setPresence(String userId) {
 * 	   	final UserId key = new UserId(userId);
 * 	   	Connection.writeStream(userId, res -> {
 * 	   		if (res.failed() || res.result() == null) {
 * 	   			setPresence(key, PresenceEnum.OFFLINE);
 * 	   		} else {
 * 	   			setPresence(key, PresenceEnum.ONLINE);
 * 	   		}
 * 	   	});
 * 	   }
 *   }
 * </pre>
 * Observe the use of the {@link Connection#writeStream(String)}}, which allows any code anywhere to send a message to a
 * connected client. The {@link #write(Envelope)} method can also be used in the {@code connected} handler.
 */
public interface Connection extends ReadStream<Envelope>, WriteStream<Envelope>, Closeable {
	Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	Map<String, Boolean> CODECS_REGISTERED = new ConcurrentHashMap<>();

	/**
	 * Retrieves a valid reference to write to a connection from anywhere, as long as the event bus on the other node is
	 * shared/clustered with this one.
	 *
	 * @param userId The user ID whose connection should be retrieved
	 * @return A connection object.
	 */
	static @NotNull
	WriteStream<Envelope> writeStream(@NotNull String userId) {
		return Vertx.currentContext().owner().eventBus().publisher(toBusAddress(userId), new DeliveryOptions().setCodecName("envelope"));
	}

	static @NotNull
	WriteStream<Envelope> writeStream(@NotNull UserId userId) {
		return writeStream(userId.toString());
	}

	/**
	 * Creates a connection object for the specified user, marking this verticle as responsible for managing this user's
	 * connection on the cluster.
	 *
	 * @param userId
	 * @return
	 */
	static Connection create(String userId) {
		String eventBusAddress = toBusAddress(userId);
		return new ConnectionImpl(userId, eventBusAddress);
	}

	static String toBusAddress(String userId) {
		return "Connection::clusteredConsumer[" + userId + "]";
	}

	/**
	 * Closes the connection, if the user has one.
	 *
	 * @param userId
	 * @param handler
	 */
	static void close(String userId, Handler<AsyncResult<Void>> handler) {
		Vertx.currentContext().owner().eventBus().send(Connection.toBusAddress(userId) + "::closer", Buffer.buffer("close"), res -> {
			if (res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
				return;
			}

			handler.handle(Future.succeededFuture());
		});
	}

	/**
	 * Creates a handler for the verticle's {@link io.vertx.ext.web.Router} that upgrades the web socket and manages the
	 * messaging over the cluster for the user.
	 *
	 * @return
	 */
	static Handler<RoutingContext> handler() {
		return suspendableHandler(Connection::connected);
	}

	static void connected(SetupHandler handler) {
		getHandlers().add(handler);
	}

	static Deque<SetupHandler> getHandlers() {
		Vertx vertx = Vertx.currentContext().owner();
		final Context context = vertx.getOrCreateContext();
		Deque<SetupHandler> handlers = context.get("Connection::handlers");

		if (handlers == null) {
			handlers = new ConcurrentLinkedDeque<>();
			context.put("Connection::handlers", handlers);
		}
		return handlers;
	}

	/**
	 * Obtains the unique lock for the user to prevent the user from calling more than one method at a time.
	 *
	 * @param userId
	 * @return
	 */
	@Suspendable
	static SuspendableLock methodLock(String userId) {
		return SuspendableLock.lock("Connection::method-ordering-lock[" + userId + "]");
	}

	/**
	 * A method that handles a routing context. For internal use only.
	 *
	 * @param routingContext
	 * @throws SuspendExecution
	 */
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
			return;
		}

		ServerWebSocket socket;

		// By the time we try to upgrade the socket, the request might have been closed anyway
		try {
			LOGGER.debug("connection {}: Upgrading socket", userId);
			socket = routingContext.request().upgrade();
		} catch (RuntimeException any) {
			LOGGER.error("connected {}: {}", userId, any.getMessage());
			routingContext.fail(403);
			return;
		}

		LOGGER.debug("connection {}: Socket upgraded", userId);

		Deque<SetupHandler> handlers = getHandlers();
		Connection connection = create(userId);

		LOGGER.debug("connection {}: Connection created", userId);
		try {
			Void ready = awaitResult(h -> connection.setSocket(socket, h));
			connection.endHandler(suspendableHandler(v -> {
				LOGGER.debug("connection {}: Connection closed", userId);
			}));
			connection.exceptionHandler(ex -> {
				LOGGER.error("connection exceptionHandler {}: {}", userId, ex.getMessage(), ex);
			});
			// All handlers should run simultaneously but we'll wait until the handlers have run
			CompositeFuture r2 = awaitResult(h -> CompositeFuture.all(handlers.stream().map(setupHandler -> {
				Future<Void> fut = Future.future();
				Vertx.currentContext().runOnContext(v -> setupHandler.handle(connection, fut));
				return fut;
			}).collect(toList())).setHandler(h));

			LOGGER.debug("connection {}: Connection ready", userId);
			// Send an envelope to indicate that the connection is ready.
			connection.write(new Envelope());
		} catch (Throwable any) {
			LOGGER.error("connected {}: {}", userId, any.getMessage(), any);
			// This also closes the socket and cleans up its handlers
			connection.close(Future.future());
		}
	}

	/**
	 * Registers the serialization codecs for messaging a client from another member of the cluster. For internal use
	 * only.
	 */
	static void registerCodecs() {
		Vertx owner = Vertx.currentContext().owner();
		if (CODECS_REGISTERED.putIfAbsent(((VertxInternal) owner).getNodeID(), true) == null) {
			owner.eventBus().registerDefaultCodec(Envelope.class, new EnvelopeMessageCodec());
		}
	}

	/**
	 * Registers the given socket to the user. For internal use only.
	 *
	 * @param socket
	 * @param readyHandler
	 */
	void setSocket(ServerWebSocket socket, Handler<AsyncResult<Void>> readyHandler);

	@Override
	default Connection exceptionHandler(Handler<Throwable> handler) {
		return null;
	}

	/**
	 * Sends a message to the client.
	 *
	 * @param data The message to send.
	 * @return This connection
	 */
	@Override
	default Connection write(@NotNull Envelope data) {
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

	/**
	 * Gets the user ID of this connection.
	 * <p>
	 * Only authorized users can have connections.
	 *
	 * @return The user ID.
	 */
	@NotNull
	String userId();

	Connection removeHandler(Handler<Envelope> handler);

	@FunctionalInterface
	interface SetupHandler extends Handler<Connection> {
		@Override
		@Suspendable
		default void handle(Connection event) {
			handle(event, Future.future());
		}

		@Suspendable
		void handle(Connection connection, Handler<AsyncResult<Void>> completionHandler);
	}
}
