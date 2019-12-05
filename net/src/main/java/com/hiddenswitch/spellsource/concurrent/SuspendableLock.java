package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.Semaphore;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * A suspendable, cluster-wide lock.
 */
public interface SuspendableLock {
	Logger LOGGER = LoggerFactory.getLogger(SuspendableLock.class);


	/**
	 * Obtains a lock that doesn't do anything.
	 *
	 * @return
	 */
	@NotNull
	static SuspendableLock noOpLock() {
		return new NoOpLock();
	}

	/**
	 * Obtains a lock using a Hazelcast cluster.
	 *
	 * @param name    The unique identifier of this lock
	 * @param timeout How long to wait for the lock. If the timeout is 0, the method waits indefinitely
	 * @return A lock that has been acquired.
	 * @throws VertxException with an inner cause of {@link TimeoutException} if the lock acquisition has timed out,
	 *                        otherwise the Hazelcast cluster experienced an error.
	 */
	@Suspendable
	@NotNull
	static SuspendableLock lock(@NotNull String name, long timeout) {
		return VertxLock.lock(name, timeout);
	}

	@Suspendable
	@NotNull
	static SuspendableLock lock(String name) {
		return VertxLock.lock(name);
	}

	@Suspendable
	void release();


	/**
	 * Destroys the lock, freeing up the resources it used in the cluster.
	 */
	@Suspendable
	void destroy();

	class VertxLock implements SuspendableLock {
		private final Lock lock;

		public VertxLock(Lock lock) {
			this.lock = lock;
		}

		@Suspendable
		@NotNull
		static VertxLock lock(String name, long timeout) {
			Lock lock = awaitResult(h -> Vertx.currentContext().owner().sharedData().getLockWithTimeout(name, timeout, h));
			return new VertxLock(lock);
		}

		@Suspendable
		@NotNull
		static VertxLock lock(String name) {
			Lock lock = awaitResult(h -> Vertx.currentContext().owner().sharedData().getLock(name, h));
			return new VertxLock(lock);
		}

		@Override
		@Suspendable
		public void release() {
			lock.release();
		}

		@Override
		@Suspendable
		public void destroy() {
		}
	}

	class NoOpLock implements SuspendableLock {

		@Override
		public void release() {
		}

		@Override
		public void destroy() {

		}
	}
}
