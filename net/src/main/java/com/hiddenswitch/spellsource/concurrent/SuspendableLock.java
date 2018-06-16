package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.ISemaphore;
import com.hiddenswitch.spellsource.util.Hazelcast;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.concurrent.TimeUnit;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static com.hiddenswitch.spellsource.util.Sync.invoke0;
import static com.hiddenswitch.spellsource.util.Sync.invoke1;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public interface SuspendableLock {
	String LOCK_SEMAPHORE_PREFIX = "__vertx.";

	@Suspendable
	static SuspendableLock lock(String name, long timeout) {
		final Vertx vertx = Vertx.currentContext().owner();
		return awaitResult(fut1 -> {
			vertx.executeBlocking(fut2 -> {
				ISemaphore iSemaphore = Hazelcast.getHazelcastInstance().getSemaphore(LOCK_SEMAPHORE_PREFIX + name);
				boolean locked = false;
				long remaining = timeout;
				do {
					long start = System.nanoTime();
					try {
						locked = iSemaphore.tryAcquire(remaining, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// OK continue
					}
					remaining = remaining - MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);
				} while (!locked && remaining > 0);
				if (locked) {
					fut2.complete(new HazelcastLock(iSemaphore));
				} else {
					throw new VertxException("Timed out waiting to get lock " + name);
				}
			}, false, fut1);
		});
	}

	@Suspendable
	void release();

	@Suspendable
	static SuspendableLock lock(String name) {
		return lock(name, Long.MAX_VALUE);
	}

	class HazelcastLock implements SuspendableLock {
		private final ISemaphore semaphore;

		private HazelcastLock(ISemaphore semaphore) {
			this.semaphore = semaphore;
		}

		@Override
		@Suspendable
		public void release() {
			final Vertx vertx = Vertx.currentContext().owner();
			invoke0(semaphore::release);
		}
	}
}
