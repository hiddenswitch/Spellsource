package com.hiddenswitch.spellsource.concurrent.impl;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.SuspendableCounter;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.shareddata.Counter;

import static com.hiddenswitch.spellsource.util.Sync.invoke;

public class SuspendableVertxCounter implements SuspendableCounter {
	private final Counter counter;

	public SuspendableVertxCounter(Counter counter) {
		this.counter = counter;
	}

	@Override
	@Suspendable
	public long get() {
		return Sync.invoke1(counter::get);
	}

	@Override
	@Suspendable
	public long incrementAndGet() {
		return Sync.invoke1(counter::incrementAndGet);
	}

	@Override
	@Suspendable
	public long getAndIncrement() {
		return Sync.invoke1(counter::getAndIncrement);
	}

	@Override
	@Suspendable
	public long decrementAndGet() {
		return Sync.invoke1(counter::decrementAndGet);
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
