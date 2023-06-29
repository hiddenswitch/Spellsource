package com.hiddenswitch.framework.virtual;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class VirtualThreadRoutingContextHandler {
	private static final InheritableThreadLocal<RoutingContext> context = new InheritableThreadLocal<>();

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
}
