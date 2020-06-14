package com.hiddenswitch.spellsource.net.concurrent;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.concurrent.impl.LocalQueue;
import com.hiddenswitch.spellsource.net.concurrent.impl.SuspendableAtomixQueue;
import io.vertx.core.Vertx;

public interface SuspendableQueue<V> extends AutoCloseable {
	/**
	 * Gets a reference to a bounded queue with the specified {@code name}.
	 * <p>
	 * If a {@code capacity > 0} is specified, the queue is bounded.
	 * <p>
	 * If the queue does not exist, it will be created when any {@link #offer(Object)} is called with {@code
	 * createQueue == true}; or, if a {@link #poll(long)} is ever called.
	 *
	 * @param name     The name of the queue shared in the cluster
	 * @param capacity The bounded capacity ({@link #offer(Object)} will return {@code false} if the bound is exceeded),
	 *                 or if {@code 0} or negative, no bound is specified.
	 * @param <V>      The type of the items.
	 * @return A reference to a suspendable queue. Until a method is called on it, its underlying data structures in the
	 * 		cluster may not be created.
	 * @see #get(String) for an unbounded version of this method.
	 */
	@Suspendable
	static <V> SuspendableQueue<V> get(String name, int capacity) throws SuspendExecution {
		if (Vertx.currentContext().owner().isClustered()) {
			return new SuspendableAtomixQueue<>(name);
		} else {
			return LocalQueue.get(name, capacity);
		}
	}

	/**
	 * Creates an unbounded (growing) queue.
	 *
	 * @param name
	 * @param <V>
	 * @return
	 * @see #get(String, int) for more options.
	 */
	@Suspendable
	static <V> SuspendableQueue<V> get(String name) throws SuspendExecution {
		if (Vertx.currentContext().owner().isClustered()) {
			return new SuspendableAtomixQueue<>(name);
		} else {
			return LocalQueue.get(name, 64);
		}
	}

	@Suspendable
	static <V> SuspendableQueue<V> getOrCreate(String name) throws SuspendExecution {
		if (Vertx.currentContext().owner().isClustered()) {
			return new SuspendableAtomixQueue<>(name);
		} else {
			return LocalQueue.getOrCreateLocalQueue(name, 64);
		}
	}

	/**
	 * Waits {@code timeout} for an item, returning it or {@code null} if none is available by that timeout.
	 * <p>
	 * Whenever a queue is polled, it is always created if it didn't previously exist.
	 *
	 * @param timeout The amount of time to wait for the queue to have an item, in milliseconds
	 * @return The item, or {@code null} if the request timed out.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	@Suspendable
	V poll(long timeout) throws InterruptedException, SuspendExecution;

	/**
	 * Takes an item from the queue, blocking until one is received.
	 *
	 * @return An item from the queue
	 * @throws InterruptedException If the take was interrupted / cancelled.
	 * @throws SuspendExecution
	 */
	@Suspendable
	default V take() throws InterruptedException, SuspendExecution {
		// Specifically use Integer.MAX_VALUE to prevent arithmetic with this timeout from overflowing in longs
		return poll(Integer.MAX_VALUE);
	}

	/**
	 * @param item The item to offer
	 * @return {@code true} if the queue was not at capacity. Always creates the queue if it didn't exist before.
	 * @see #offer(Object) for more on sending.
	 */
	@Suspendable
	boolean offer(V item);

	/**
	 * Destroys the queue.
	 * <p>
	 * Destroying the queue does <b>not</b> interrupt existing calls to {@link #poll(long)}.
	 * <p>
	 * Once the queue is destroyed, its backing datastructures in the cluster are removed.
	 */
	@Suspendable
	default void destroy() {
	}

	/**
	 * To the best of the cluster's knowledge, does the specified queue exist?
	 *
	 * @param name
	 * @return {@code true} if it does
	 */
	@Suspendable
	static boolean exists(String name) {
		if (Vertx.currentContext().owner().isClustered()) {
			throw new UnsupportedOperationException();
		} else {
			return LocalQueue.containsKey(name);
		}
	}

	@Override
	@Suspendable
	void close();
}