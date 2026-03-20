package com.hiddenswitch.framework.virtual;

import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

public class VirtualThreadRoutingContextHandler {
	private static final InheritableThreadLocal<RoutingContext> context = new InheritableThreadLocal<>();
	private static final InheritableThreadLocal<User> userOverride = new InheritableThreadLocal<>();

	public static Handler<RoutingContext> create(Handler<RoutingContext> toDecorate) {
		return ctx -> {
			if (!Thread.currentThread().isVirtual()) {
				throw new IllegalStateException();
			}
			context.set(ctx);
			toDecorate.handle(ctx);
		};
	}

	public static RoutingContext current() {
		return context.get();
	}

	/**
	 * Set an authenticated user for contexts where there is no RoutingContext
	 * (e.g. WebSocket subscription handlers). Cleared with {@link #clearUser()}.
	 */
	public static void setUser(User user) {
		userOverride.set(user);
	}

	public static void clearUser() {
		userOverride.remove();
	}

	/**
	 * Returns the authenticated user, checking the user override first (for WebSocket
	 * contexts) then falling back to the routing context user.
	 */
	public static User user() {
		var override = userOverride.get();
		if (override != null) {
			return override;
		}
		var ctx = context.get();
		return ctx != null ? ctx.user() : null;
	}
}
