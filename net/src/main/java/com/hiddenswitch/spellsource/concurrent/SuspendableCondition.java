package com.hiddenswitch.spellsource.concurrent;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.impl.SuspendableEventBusCondition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Closeable;
import io.vertx.core.Handler;

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
	void awaitMillis(long millis, Handler<AsyncResult<Void>> handler);

	@Suspendable
	void signal();

	@Suspendable
	void signalAll();
}
