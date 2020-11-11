package com.hiddenswitch.spellsource.net.concurrent.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableCounter;
import io.vertx.core.Closeable;
import io.vertx.core.shareddata.Counter;

import static io.vertx.ext.sync.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitPromise;
import static io.vertx.ext.sync.Sync.await;

public class SuspendableVertxCounter implements SuspendableCounter {
	private final Counter counter;

	public SuspendableVertxCounter(Counter counter) {
		this.counter = counter;
	}

	@Override
	@Suspendable
	public long get() {
		return io.vertx.ext.sync.Sync.invoke1(counter::get);
	}

	@Override
	@Suspendable
	public long incrementAndGet() {
		return io.vertx.ext.sync.Sync.invoke1(counter::incrementAndGet);
	}

	@Override
	@Suspendable
	public long getAndIncrement() {
		return io.vertx.ext.sync.Sync.invoke1(counter::getAndIncrement);
	}

	@Override
	@Suspendable
	public long decrementAndGet() {
		return io.vertx.ext.sync.Sync.invoke1(counter::decrementAndGet);
	}

	@Override
	@Suspendable
	public long addAndGet(long value) {
		return io.vertx.ext.sync.Sync.await(counter.addAndGet(value));
	}

	@Override
	@Suspendable
	public long getAndAdd(long value) {
		return io.vertx.ext.sync.Sync.await(counter.getAndAdd(value));
	}

	@Override
	@Suspendable
	public boolean compareAndSet(long expected, long value) {
		return io.vertx.ext.sync.Sync.await(counter.compareAndSet(expected, value));
	}

	@Override
	@Suspendable
	public void close() {
		if (counter instanceof Closeable) {
			Void t = awaitPromise(((Closeable) counter)::close);
		}
	}
}
