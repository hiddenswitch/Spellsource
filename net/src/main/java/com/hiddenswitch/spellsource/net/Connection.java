package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.net.impl.*;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * Manages the real time data connection users get when they connect to the Spellsource server.
 * <p>
 * To set up a behaviour that uses the real time connect, use {@link Connection#connected(SetupHandler)}, which passes
 * you a new connection to a unique user.
 */
public interface Connection extends ReadStream<Envelope>, WriteStream<Envelope>, Closeable {
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
		return Vertx.currentContext().owner().eventBus().publisher(toBusAddress(userId));
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
		return "Connection/clusteredConsumer/" + userId;
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

	/**
	 * Configures a handler for methods and notifications for the client.
	 * <p>
	 * A common pattern is to do things to the connection object. Once you are done setting up, make sure to call
	 * {@code fut.handle(Future.succeededFuture());}.
	 * <pre>
	 *   {@code
	 *   		Connection.connected((connection, fut) -> {
	 * 			  connection.handler(suspendableHandler(env -> {
	 * 			  	// Check the contents of env.getMethod() for methods
	 *        }));
	 * 			  fut.handle(Future.succeededFuture());
	 *      });
	 *   }
	 * </pre>
	 * You can use {@link Connection#writeStream(String)}}, which allows any code anywhere to send a message to a
	 * connected client. The {@link #write(Envelope)} method can also be used in the {@code connected} handler.
	 *
	 * @param handler a setup handler
	 */
	static void connected(SetupHandler handler) {
		getHandlers().add(handler);
	}

	static Deque<SetupHandler> getHandlers() {
		Vertx vertx = Vertx.currentContext().owner();
		final Context context = vertx.getOrCreateContext();
		Deque<SetupHandler> handlers = context.get("Connection/handlers");

		if (handlers == null) {
			handlers = new ConcurrentLinkedDeque<>();
			context.put("Connection/handlers", handlers);
		}
		return handlers;
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

		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Connection/connected")
				.withTag("userId", userId)
				.start();
		SpanContext spanContext = span.context();

		// By the time we try to upgrade the socket, the request might have been closed anyway
		try {
			span.log("upgrading");
			socket = routingContext.request().upgrade();
			span.log("upgraded");
		} catch (RuntimeException any) {
			routingContext.fail(403);
			return;
		}

		Deque<SetupHandler> handlers = getHandlers();
		Connection connection = create(userId);

		try {
			Void ready = awaitResult(h -> {
				connection.setSocket(socket, h, spanContext);
				span.log("ready");
			});

			connection.endHandler(v -> span.finish());
			connection.exceptionHandler(ex -> {
				// Wrap this so we can see where it actually occurs
				if (!(ex instanceof IOException)) {
					Tracing.error(new VertxException(ex), span, false);
				}
				connection.close(Promise.promise());
			});
			// All handlers should run simultaneously but we'll wait until the handlers have run
			CompositeFuture r2 = awaitResult(h -> CompositeFuture.all(handlers.stream().map(setupHandler -> {
				Promise<Void> fut = Promise.promise();
				Vertx.currentContext().runOnContext(v -> {
					span.log("setupHandler");
					setupHandler.handle(connection, fut);
				});
				return fut.future();
			}).collect(toList())).setHandler(h));
			// Send an envelope to indicate that the connection is ready.
			connection.write(new Envelope());
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			// This also closes the socket and cleans up its handlers
			connection.close(Promise.promise());
		}
	}

	/**
	 * Registers the serialization codecs for messaging a client from another member of the cluster. For internal use
	 * only.
	 */
	static void registerCodecs() {
		Vertx owner = Vertx.currentContext().owner();
		String nodeId;

		if (((VertxInternal) owner).getClusterManager() == null) {
			nodeId = owner.toString();
		} else {
			nodeId = ((VertxInternal) owner).getNodeID();
		}

		if (CODECS_REGISTERED.putIfAbsent(nodeId, true) == null) {
			owner.eventBus().registerDefaultCodec(Envelope.class, new EnvelopeMessageCodec());
			owner.eventBus().registerDefaultCodec(ServerToClientMessage.class, new ServerToClientMessageCodec());
			owner.eventBus().registerDefaultCodec(ClientToServerMessage.class, new ClientToServerMessageCodec());
		}
	}

	/**
	 * Registers the given socket to the user. For internal use only.
	 *
	 * @param socket
	 * @param readyHandler
	 * @param spanContext  An optional SpanContext for instrumentation
	 */
	void setSocket(ServerWebSocket socket, Handler<AsyncResult<Void>> readyHandler, @Nullable SpanContext spanContext);

	@Override
	default Connection exceptionHandler(Handler<Throwable> handler) {
		return this;
	}

	/**
	 * Sends a message to the client.
	 *
	 * @param data The message to send.
	 * @return This connection
	 */
	@Override
	default Connection write(@NotNull Envelope data) {
		return this;
	}

	@Override
	default void end() {
	}

	@Override
	default Connection setWriteQueueMaxSize(int maxSize) {
		return this;
	}

	@Override
	default boolean writeQueueFull() {
		return false;
	}

	@Override
	default Connection drainHandler(Handler<Void> handler) {
		return this;
	}

	@Override
	default Connection handler(Handler<Envelope> handler) {
		return this;
	}

	@Override
	default Connection pause() {
		return this;
	}

	@Override
	default Connection resume() {
		return this;
	}

	@Override
	default Connection endHandler(Handler<Void> endHandler) {
		return this;
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
			handle(event, Promise.promise());
		}

		@Suspendable
		void handle(Connection connection, Handler<AsyncResult<Void>> completionHandler);
	}
}
