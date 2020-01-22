package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.impl.TimerId;
import io.vertx.core.Handler;

public interface Scheduler {
	@Suspendable
	TimerId setTimer(long delay, Handler<Long> handler);

	@Suspendable
	TimerId setInterval(long delay, Handler<Long> handler);

	@Suspendable
	boolean cancelTimer(TimerId id);
}
