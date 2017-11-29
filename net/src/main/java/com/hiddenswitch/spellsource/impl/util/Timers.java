package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;

public interface Timers {
	@Suspendable
	long setTimer(long delay, Handler<Long> handler);

	@Suspendable
	boolean cancelTimer(long id);
}
