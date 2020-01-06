package com.hiddenswitch.spellsource.net.concurrent.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableCounter;
import com.hiddenswitch.spellsource.net.impl.Sync;
import io.vertx.core.Closeable;
import io.vertx.core.shareddata.Counter;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;

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

	@Override
	@Suspendable
	public void close() {
		if (counter instanceof Closeable) {
			Void t = io.vertx.ext.sync.Sync.awaitResult(((Closeable) counter)::close);
		}
	}
}
