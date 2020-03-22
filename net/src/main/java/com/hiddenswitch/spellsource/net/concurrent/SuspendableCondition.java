package com.hiddenswitch.spellsource.net.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.impl.SuspendableEventBusCondition;
import io.vertx.core.Closeable;

public interface SuspendableCondition extends Closeable {

	static SuspendableCondition getOrCreate(String name) {
		return new SuspendableEventBusCondition(name);
	}

	/**
	 * Awaits until the condition is signalled.
	 *
	 * @return {@code true} if a signal was received while waiting, or {@code false} if interrupted.
	 */
	@Suspendable
	boolean await();

	@Suspendable
	long awaitMillis(long millis);

	@Suspendable
	void signalAll();
}
