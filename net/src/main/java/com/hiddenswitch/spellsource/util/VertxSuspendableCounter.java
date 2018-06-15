package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.shareddata.Counter;

import static com.hiddenswitch.spellsource.util.Sync.invoke;

public class VertxSuspendableCounter implements SuspendableCounter {
	private final Counter counter;

	VertxSuspendableCounter(Counter counter) {
		this.counter = counter;
	}

	@Override
	@Suspendable
	public long get() {
		return invoke(counter::get);
	}

	@Override
	@Suspendable
	public long incrementAndGet() {
		return invoke(counter::incrementAndGet);
	}

	@Override
	@Suspendable
	public long getAndIncrement() {
		return invoke(counter::getAndIncrement);
	}

	@Override
	@Suspendable
	public long decrementAndGet() {
		return invoke(counter::decrementAndGet);
	}

	@Override
	@Suspendable
	public long addAndGet(long value) {
		return invoke(counter::addAndGet, value);
	}

	@Override
	@Suspendable
	public long getAndAdd(long value) {
		return invoke(counter::getAndAdd, value);
	}

	@Override
	@Suspendable
	public boolean compareAndSet(long expected, long value) {
		return invoke(counter::compareAndSet, expected, value);
	}
}
