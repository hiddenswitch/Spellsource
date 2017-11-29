package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.util.Timers;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class VertxTimers implements Timers {
	final Vertx vertx;

	public VertxTimers(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	@Suspendable
	public long setTimer(long delay, Handler<Long> handler) {
		return vertx.setTimer(delay, handler);
	}

	@Override
	@Suspendable
	public boolean cancelTimer(long id) {
		return vertx.cancelTimer(id);
	}
}
