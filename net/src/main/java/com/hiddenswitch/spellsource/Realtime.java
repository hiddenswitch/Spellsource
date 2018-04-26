package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.util.SharedData;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.*;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Realtime {
	private static Logger logger = LoggerFactory.getLogger(SharedData.class);

	public static Handler<RoutingContext> create() {
		return Sync.suspendableHandler(Realtime::connected);
	}

	public static void connected(Handler<Connection> handler) {
		getHandlers().add(handler);
	}

	private static Deque<Handler<Connection>> getHandlers() {
		Vertx vertx = Vertx.currentContext().owner();
		final Context context = vertx.getOrCreateContext();
		Deque<Handler<Connection>> handlers = context.get("Realtime::handlers");

		if (handlers == null) {
			handlers = new ConcurrentLinkedDeque<>();
			context.put("Realtime::handlers", handlers);
		}
		return handlers;
	}

	@Suspendable
	private static void connected(RoutingContext routingContext) throws SuspendExecution {
		String userId = Accounts.userId(routingContext);
		Lock lock;

		try {
			lock = SharedData.lock("Realtime::lock-" + userId, 200L);
		} catch (VertxException ex) {
			routingContext.fail(ex);
			return;
		}

		ServerWebSocket socket = routingContext.request().upgrade();
		Deque<Handler<Connection>> handlers = getHandlers();
		Connection connection = Connection.create(socket, userId);

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
}
