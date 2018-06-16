package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import org.jetbrains.annotations.NotNull;

import static io.vertx.ext.sync.Sync.awaitResult;

public interface SuspendableQueue<V> {
	/**
	 * Gets a reference to a bounded queue with the specified {@code name}.
	 * <p>
	 * If a {@code capacity > 0} is specified, the queue is bounded.
	 * <p>
	 * If the queue does not exist, it will be created when any {@link #offer(Object, boolean)} is called with {@code
	 * createQueue == true}; or, if a {@link #poll(long)} is ever called.
	 *
	 * @param name     The name of the queue shared in the cluster
	 * @param capacity The bounded capacity ({@link #offer(Object)} will return {@code false} if the bound is exceeded),
	 *                 or if {@code 0} or negative, no bound is specified.
	 * @param <V>      The type of the items.
	 * @return A reference to a suspendable queue. Until a method is called on it, its underlying data structures in the
	 * cluster may not be created.
	 * @see #get(String) for an unbounded version of this method.
	 */
	static <V> SuspendableQueue<V> get(String name, int capacity) throws SuspendExecution {
		return SuspendableLinkedQueue.getOrCreate(name, capacity <= 0 ? Integer.MAX_VALUE : capacity);
//		return new SuspendableArrayQueue<>(name, capacity);
	}

	/**
	 * Creates an unbounded (growing) queue.
	 *
	 * @param name
	 * @param <V>
	 * @return
	 * @see #get(String, int) for more options.
	 */

	static <V> SuspendableQueue<V> get(String name) throws SuspendExecution {
//		return new SuspendableArrayQueue<>(name);
		return SuspendableLinkedQueue.getOrCreate(name);
	}

	@Suspendable
	boolean offer(@NotNull V item, boolean createQueue);

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
	V poll(long timeout) throws InterruptedException, SuspendExecution;

	default V take() throws InterruptedException, SuspendExecution {
		return poll(Long.MAX_VALUE);
	}

	/**
	 * @param item The item to offer
	 * @return {@code true} if the queue was not at capacity. Always creates the queue if it didn't exist before.
	 * @see #offer(Object) for more on sending.
	 */
	@Suspendable
	default boolean offer(V item) {
		return offer(item, true);
	}

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
}