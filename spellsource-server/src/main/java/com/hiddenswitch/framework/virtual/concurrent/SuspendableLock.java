package com.hiddenswitch.framework.virtual.concurrent;

import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

import static io.vertx.await.Async.await;

/**
 * A suspendable, cluster-wide lock.
 */
public interface SuspendableLock extends Closeable {
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
	 * Obtains a lock using the cluster configured for Vertx.
	 *
	 * @param name    The unique identifier of this lock
	 * @param timeout How long to wait for the lock. If the timeout is 0, the method waits indefinitely
	 * @return A lock that has been acquired.
	 * @throws VertxException with an inner cause of {@link TimeoutException} if the lock acquisition has timed out,
	 *                        otherwise the Hazelcast cluster experienced an error.
	 */
	@NotNull
	static SuspendableLock lock(@NotNull String name, long timeout) throws VertxException {
		return VertxLock.lock(name, timeout);
	}

	@NotNull
	static SuspendableLock lock(String name) {
		return VertxLock.lock(name);
	}

	void release();


	/**
	 * Destroys the lock, freeing up the resources it used in the cluster.
	 */
	void destroy();

	class VertxLock implements SuspendableLock {
		private final Lock lock;

		public VertxLock(Lock lock) {
			this.lock = lock;
		}

		@NotNull
		static VertxLock lock(String name, long timeout) {
			var context = Vertx.currentContext();
			Lock lock = await(context.owner().sharedData().getLockWithTimeout(name, timeout));
			return new VertxLock(lock);
		}

		@NotNull
		static VertxLock lock(String name) {
			var context = Vertx.currentContext();
			Lock lock = await(context.owner().sharedData().getLock(name));
			return new VertxLock(lock);
		}

		@Override
		public void release() {
			lock.release();
		}

		@Override
		public void destroy() {
		}

		@Override
		public void close(Promise<Void> promise) {
			release();
			promise.complete();
		}
	}

	class NoOpLock implements SuspendableLock {

		@Override
		public void release() {
		}

		@Override
		public void destroy() {

		}

		@Override
		public void close(Promise<Void> promise) {
			promise.complete();
		}
	}
}
