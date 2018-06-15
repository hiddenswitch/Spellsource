package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;

public interface SuspendableCondition extends Closeable {

	static SuspendableCondition getOrCreate(String name) {
		return new SuspendableEventBusCondition(name);
	}

	@Suspendable
	long awaitMillis(long millis);

	@Suspendable
	void awaitMillis(long millis, Handler<AsyncResult<Void>> handler);

	@Suspendable
	void signal();

	@Suspendable
	void signalAll();
}
