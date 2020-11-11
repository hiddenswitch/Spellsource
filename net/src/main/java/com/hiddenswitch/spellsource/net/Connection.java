package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.base.Throwables;
import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.*;
import io.opentracing.SpanContext;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.vertx.core.http.HttpHeaders.*;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.toList;

/**
 * Manages the real time data connection users get when they connect to the Spellsource server.
 * <p>
 * To set up a behaviour that uses the real time connect, use {@link Connection#connected(String, SetupHandler)}, which
 * passes you a new connection to a unique user.
 */
public interface Connection extends ReadStream<Envelope>, WriteStream<Envelope>, Closeable {
	Set<EventBus> CODECS_REGISTERED = Collections.newSetFromMap(new WeakHashMap<>());
	Map<String, SetupHandler> HANDLERS = new ConcurrentHashMap<>();

	/**
	 * Retrieves a valid reference to write to a connection from anywhere, as long as the event bus on the other node is
	 * shared/clustered with this one.
	 *
	 * @param userId The user ID whose connection should be retrieved
	 * @return A connection object.
	 */
	static @NotNull
	MessageProducer<Envelope> writeStream(@NotNull String userId) {
		return Vertx.currentContext().owner().eventBus().<Envelope>publisher(toBusAddress(userId));
	}

	static @NotNull
	MessageProducer<Envelope> writeStream(@NotNull UserId userId) {
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
		var eventBusAddress = toBusAddress(userId);
		return new ConnectionImpl(userId, eventBusAddress);
	}

	static String toBusAddress(String userId) {
		return "Connection.clusteredConsumer." + userId;
	}

	/**
	 * Creates a handler for the verticle's {@link io.vertx.ext.web.Router} that upgrades the web socket and manages the
	 * messaging over the cluster for the user.
	 *
	 * @return
	 */
	static Handler<RoutingContext> handler() {
		return fiber(Connection::connected);
	}

	/**
	 * Configures a handler for methods and notifications for the client.
	 * <p>
	 * A common pattern is to do things to the connection object. Once you are done setting up, make sure to call {@code
	 * fut.handle(Future.succeededFuture());}.
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
	static void connected(String name, SetupHandler handler) {
		HANDLERS.putIfAbsent(name, handler);
	}

	static Collection<SetupHandler> getHandlers() {
		return HANDLERS.values();
	}

	/**
	 * A method that handles a routing context. For internal use only.
	 *
	 * @param routingContext
	 */
	@Suspendable
	static void connected(RoutingContext routingContext) {
		var headers = routingContext.request().headers();
		if (!(headers.contains(CONNECTION) && headers.contains(UPGRADE, WEBSOCKET, true))) {
			routingContext.next();
			return;
		}

		if (routingContext.failed()) {
			routingContext.next();
			return;
		}

		ServerWebSocket socket;

		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("Connection/connected")
				.start();
		var spanContext = span.context();

		// By the time we try to upgrade the socket, the request might have been closed anyway
		try {
			span.log("upgrading");
			socket = await(routingContext.request().toWebSocket());
			span.log("upgraded");
		} catch (RuntimeException any) {
			Tracing.error(any, span, true);
			routingContext.fail(403);
			return;
		}

		String userId;
		try {
			SpellsourceAuthHandler.authorize(routingContext);
			userId = Accounts.userId(routingContext);
		} catch (Throwable t) {
			socket.close();
			routingContext.fail(403);
			return;
		}


		registerCodecs();

		if (userId == null) {
			socket.close();
			routingContext.fail(403);
			return;
		}

		span.setTag("userId", userId);

		var handlers = getHandlers();
		var connection = create(userId);

		try {
			Void ready = await(h -> {
				connection.setSocket(socket, h, spanContext);
				span.log("ready");
			});

			connection.addCloseHandler(fut -> {
				span.finish();
				fut.complete();
			});
			connection.exceptionHandler(ex -> {
				// Wrap this so we can see where it actually occurs
				if (!(Throwables.getRootCause(ex) instanceof IOException)) {
					Tracing.error(ex, span, false);
				}
				connection.close(Promise.promise());
			});
			// All handlers should run simultaneously but we'll wait until the handlers have run
			CompositeFuture r2 = await(h -> CompositeFuture.all(handlers.stream().map(setupHandler -> {
				Promise<Void> fut = Promise.promise();
				Vertx.currentContext().runOnContext(fiber(v -> {
					span.log("setupHandler");
					setupHandler.handle(connection, fut);
				}));
				return fut.future();
			}).collect(toList())).onComplete(h));
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
		synchronized (CODECS_REGISTERED) {
			var owner = Vertx.currentContext().owner();

			var eventBus = owner.eventBus();
			if (CODECS_REGISTERED.add(eventBus)) {
				eventBus.registerDefaultCodec(Envelope.class, new EnvelopeMessageCodec());
				eventBus.registerDefaultCodec(ServerToClientMessage.class, new ServerToClientMessageCodec());
				eventBus.registerDefaultCodec(ClientToServerMessage.class, new ClientToServerMessageCodec());
				eventBus.registerDefaultCodec(MatchmakingQueueEntry.class, new MatchmakingQueueEntryCodec());
			}
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

	@Override
	default Future<Void> write(Envelope envelope) {
		return Future.failedFuture(new NullPointerException("this"));
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

	default Connection addCloseHandler(Handler<Promise<Void>> closeHandler) {
		return this;
	}

	@Override
	default ReadStream<Envelope> endHandler(@io.vertx.codegen.annotations.Nullable Handler<Void> endHandler) {
		throw new UnsupportedOperationException("should not schedule things to happen when the user closes the connection");
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

	boolean isOpen();

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
