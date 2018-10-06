package com.hiddenswitch.spellsource.concurrent;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.impl.SuspendableVertxCounter;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;

import static io.vertx.ext.sync.Sync.awaitResult;

public interface SuspendableCounter {

	@Suspendable
	static SuspendableCounter create(String name) {
		Counter counter = awaitResult(h -> Vertx.currentContext().owner().sharedData().getCounter(name, h));
		return new SuspendableVertxCounter(counter);
	}

	/**
	 * Get the current value of the counter
	 */
	@Suspendable
	long get();

	/**
	 * Increment the counter atomically and return the new count
	 */
	@Suspendable
	long incrementAndGet();

	/**
	 * Increment the counter atomically and return the value before the increment.
	 */
	@Suspendable
	long getAndIncrement();

	/**
	 * Decrement the counter atomically and return the new count
	 */
	@Suspendable
	long decrementAndGet();

	/**
	 * Add the value to the counter atomically and return the new count
	 *
	 * @param value the value to add
	 */
	@Suspendable
	long addAndGet(long value);

	/**
	 * Add the value to the counter atomically and return the value before the add
	 *
	 * @param value the value to add
	 */
	@Suspendable
	long getAndAdd(long value);

	/**
	 * Set the counter to the specified value only if the current value is the expectec value. This happens atomically.
	 *
	 * @param expected the expected value
	 * @param value    the new value
	 */
	@Suspendable
	boolean compareAndSet(long expected, long value);

}
