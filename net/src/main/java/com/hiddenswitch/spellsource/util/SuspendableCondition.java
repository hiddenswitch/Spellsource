package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Closeable;

public interface SuspendableCondition extends Closeable {

	static SuspendableCondition create(String name) {
		return new SuspendableEventBusCondition(name);
	}

	@Suspendable
	long awaitMillis(long millis);

	@Suspendable
	void signal();
}
