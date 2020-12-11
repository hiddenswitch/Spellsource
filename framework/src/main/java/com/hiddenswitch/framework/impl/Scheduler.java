package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;

public interface Scheduler {
	@Suspendable
	Long setTimer(long delay, Handler<Long> handler);

	@Suspendable
	Long setInterval(long delay, Handler<Long> handler);

	@Suspendable
	boolean cancelTimer(Long id);
}
