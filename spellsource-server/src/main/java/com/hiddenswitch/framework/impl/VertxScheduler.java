package com.hiddenswitch.framework.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class VertxScheduler implements Scheduler {
	final Vertx vertx;

	public VertxScheduler(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public Long setTimer(long delay, Handler<Long> handler) {
		return vertx.setTimer(delay, handler);
	}

	@Override
	public Long setInterval(long delay, Handler<Long> handler) {
		return vertx.setPeriodic(delay, handler);
	}

	@Override
	public boolean cancelTimer(Long id) {
		return vertx.cancelTimer(id);
	}
}
