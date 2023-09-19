/**
 * Copyright 2013 Niall Gallagher
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.googlecode.concurentlocks;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Utility methods to group-lock and group-unlock collections of locks, including roll back support to
 * ensure that either all locks are acquired or no locks are acquired.
 *
 * @author Niall Gallagher
 */
public class Locks {

	/**
	 * Calls {@link java.util.concurrent.locks.Lock#lock()} on all locks provided by the given iterable, in the order
	 * provided by the iterable. Automatically releases any locks acquired (in reverse order) by calling
	 * {@link java.util.concurrent.locks.Lock#unlock()} if an exception is thrown, before re-throwing the exception.
	 *
	 * @param locks The locks to acquire
	 * @param <L> Type of the lock
	 */
	public static <L extends Lock> void lockAll(Iterable<L> locks) {
		Deque<L> stack = new LinkedList<L>();
		try {
			for (L lock : locks) {
				lock.lock();
				stack.push(lock);
			}
		} catch (RuntimeException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		}
	}

	/**
	 * Calls {@link java.util.concurrent.locks.Lock#lockInterruptibly()} on all locks provided by the given iterable, in
	 * the order provided by the iterable. Automatically releases any locks acquired (in reverse order) by calling
	 * {@link java.util.concurrent.locks.Lock#unlock()} if the thread is interrupted while waiting for a lock or if
	 * an exception is thrown, before re-throwing the exception.
	 *
	 * @param locks The locks to acquire
	 * @param <L> Type of the lock
	 * @throws InterruptedException If the thread is interrupted while waiting for a lock
	 */
	public static <L extends Lock> void lockInterruptiblyAll(Iterable<L> locks) throws InterruptedException {
		Deque<L> stack = new LinkedList<L>();
		try {
			for (L lock : locks) {
				lock.lockInterruptibly();
				stack.push(lock);
			}
		} catch (InterruptedException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		} catch (RuntimeException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		}
	}

	/**
	 * Calls {@link java.util.concurrent.locks.Lock#tryLock()} on all locks provided by the given iterable, in the order
	 * provided by the iterable. Automatically releases any locks acquired (in reverse order) by calling
	 * {@link java.util.concurrent.locks.Lock#unlock()} if it is not possible to obtain any lock, if the thread is
	 * interrupted while waiting for a lock, or if an exception is thrown, before re-throwing the exception.
	 *
	 * @param locks The locks to acquire
	 * @param <L> Type of the lock
	 * @return True if at least one lock was supplied and all supplied locks were acquired successfully, otherwise false
	 */
	public static <L extends Lock> boolean tryLockAll(Iterable<L> locks) {
		Deque<L> stack = new LinkedList<L>();
		boolean success = false;
		try {
			for (L lock : locks) {
				success = lock.tryLock();
				if (success) {
					stack.push(lock);
				} else {
					break;
				}
			}
		} catch (RuntimeException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		}
		if (!success) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
		}
		return success;
	}

	/**
	 * Calls {@link java.util.concurrent.locks.Lock#tryLock()} on all locks provided by the given iterable, in the order
	 * provided by the iterable. Automatically releases any locks acquired (in reverse order) by calling
	 * {@link java.util.concurrent.locks.Lock#unlock()} if it is not possible to obtain any lock within the remaining
	 * time within the timeout given, if the thread is interrupted while waiting for a lock, or if an exception is
	 * thrown, before re-throwing the exception.
	 *
	 * @param time the maximum time to wait for all locks combined
	 * @param unit the time unit of the {@code time} argument
	 * @param locks The locks to acquire
	 * @param <L> Type of the lock
	 * @return True if at least one lock was supplied and all supplied locks were acquired successfully, otherwise false
	 * @throws InterruptedException If the thread is interrupted while waiting for a lock
	 */
	public static <L extends Lock> boolean tryLockAll(long time, TimeUnit unit, Iterable<L> locks) throws InterruptedException {
		Deque<L> stack = new LinkedList<L>();
		boolean success = false;
		try {
			long limitNanos = unit.toNanos(time);
			long startNanos = System.nanoTime();
			for (L lock : locks) {
				long remainingNanos = !success
						? limitNanos // No need to calculate remaining time in first iteration
						: limitNanos - (System.nanoTime() - startNanos); // recalculate in subsequent iterations

				// Note if remaining time is <= 0, we still try to obtain additional locks, supplying zero or negative
				// timeouts to those locks, which should treat it as a non-blocking tryLock() per API docs...
				success = lock.tryLock(remainingNanos, TimeUnit.NANOSECONDS);
				if (success) {
					stack.push(lock);
				} else {
					break;
				}
			}
		} catch (RuntimeException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		} catch (InterruptedException e) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
			throw e;
		}
		if (!success) {
			// Roll back: unlock the locks acquired so far...
			unlockAll(stack);
		}
		return success;
	}

	/**
	 * Calls {@link java.util.concurrent.locks.Lock#unlock()} on all locks provided by the given iterable, in the
	 * <b>order provided</b> by the iterable. <b>Note you may therefore wish to supply locks in reverse order.</b>
	 *
	 * @param locks The locks to unlock
	 * @param <L> Type of the lock
	 */
	public static <L extends Lock> void unlockAll(Iterable<L> locks) {
		for (L lock : locks) {
			lock.unlock();
		}
	}

	// ***************************
	// *** Varargs variants... ***
	// ***************************

	/**
	 * Varargs variant of {@link #lockAll(Iterable)}
	 * @see #lockAll(Iterable)
	 */
	@SuppressWarnings({"JavaDoc"})
	public static <L extends Lock> void lockAll(L... locks) {
		lockAll(Arrays.asList(locks));
	}

	/**
	 * Varargs variant of {@link #lockInterruptiblyAll(Iterable)}
	 * @see #lockInterruptiblyAll(Iterable)
	 */
	@SuppressWarnings({"JavaDoc"})
	public static <L extends Lock> void lockInterruptiblyAll(L... locks) throws InterruptedException {
		lockInterruptiblyAll(Arrays.asList(locks));
	}

	/**
	 * Varargs variant of {@link #tryLockAll(Iterable)}
	 * @see #tryLockAll(Iterable)
	 */
	@SuppressWarnings({"JavaDoc"})
	public static <L extends Lock> boolean tryLockAll(L... locks) {
		return tryLockAll(Arrays.asList(locks));
	}

	/**
	 * Varargs variant of {@link #tryLockAll(long, java.util.concurrent.TimeUnit, Iterable)}
	 * @see #tryLockAll(long, java.util.concurrent.TimeUnit, Iterable)
	 */
	@SuppressWarnings({"JavaDoc"})
	public static <L extends Lock> boolean tryLockAll(long time, TimeUnit unit, L... locks) throws InterruptedException {
		return tryLockAll(time, unit, Arrays.asList(locks));
	}

	/**
	 * Varargs variant of {@link #unlockAll(Iterable)}
	 * @see #unlockAll(Iterable)
	 */
	@SuppressWarnings({"JavaDoc"})
	public static <L extends Lock> void unlockAll(L... locks) {
		unlockAll(Arrays.asList(locks));
	}

	/**
	 * Private constructor, not used.
	 */
	Locks() {
	}
}
