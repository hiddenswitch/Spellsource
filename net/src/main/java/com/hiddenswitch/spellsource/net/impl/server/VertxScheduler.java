package com.hiddenswitch.spellsource.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.impl.TimerId;
import com.hiddenswitch.spellsource.net.impl.util.Scheduler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class VertxScheduler implements Scheduler {
	final Vertx vertx;

	public VertxScheduler(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	@Suspendable
	public TimerId setTimer(long delay, Handler<Long> handler) {
		return new TimerId(vertx.setTimer(delay, handler));
	}

	@Override
	@Suspendable
	public TimerId setInterval(long delay, Handler<Long> handler) {
		return new TimerId(vertx.setPeriodic(delay, handler));
	}

	@Override
	@Suspendable
	public boolean cancelTimer(TimerId id) {
		return vertx.cancelTimer(id.longValue());
	}
}
