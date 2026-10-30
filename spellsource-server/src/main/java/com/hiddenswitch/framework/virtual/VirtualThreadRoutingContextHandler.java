package com.hiddenswitch.framework.virtual;

import com.hiddenswitch.diagnostics.Tracing;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

public class VirtualThreadRoutingContextHandler {
	private static final InheritableThreadLocal<RoutingContext> context = new InheritableThreadLocal<>();
	private static final InheritableThreadLocal<User> userOverride = new InheritableThreadLocal<>();
	private static final java.util.regex.Pattern GRAPHQL_OP_PATTERN =
			java.util.regex.Pattern.compile("^\\s*(?:mutation|query|subscription)\\s+([A-Za-z0-9_]+)");

	public static Handler<RoutingContext> create(Handler<RoutingContext> toDecorate) {
		return ctx -> {
			if (!Thread.currentThread().isVirtual()) {
				throw new IllegalStateException();
			}
			context.set(ctx);
			var parent = ctx.request() != null ? Tracing.extractFromHeaders(ctx.request().headers()) : null;
			var operationName = "GraphQL/request";
			try {
				var bodyJson = ctx.body() != null ? ctx.body().asJsonObject() : null;
				if (bodyJson != null) {
					var opName = bodyJson.getString("operationName");
					if (opName == null || opName.isBlank()) {
						var query = bodyJson.getString("query");
						if (query != null) {
							var matcher = GRAPHQL_OP_PATTERN.matcher(query);
							if (matcher.find()) {
								opName = matcher.group(1);
							}
						}
					}
					if (opName != null && !opName.isBlank()) {
						operationName = "GraphQL/" + opName;
					}
				}
			} catch (Exception ignored) {
			}
			var span = Tracing.tracing().buildSpan(operationName).asChildOf(parent).start();
			ctx.addEndHandler(v -> span.finish());
			try (var ignored = Tracing.tracing().activateSpan(span)) {
				toDecorate.handle(ctx);
			}
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
