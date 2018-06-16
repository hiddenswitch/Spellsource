package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Lock;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitResult;

public interface SuspendableLock {

	@Suspendable
	static Lock lock(String name, long timeout) {
		final Vertx vertx = Vertx.currentContext().owner();
		return awaitResult(h -> vertx.sharedData().getLockWithTimeout(name, timeout, h));
	}

	@Suspendable
	static Lock lock(String name) {
		return invoke(Vertx.currentContext().owner().sharedData()::getLockWithTimeout, name, Long.MAX_VALUE);
	}
}
