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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A lock spanning a group of backing locks. When this composite lock is locked, it locks all backing locks, and when
 * unlocked, it unlocks all backing locks. Employs roll back logic to ensure that either all locks are acquired
 * or no locks are acquired. Locks are unlocked in the reverse of the order in which the were acquired.
 * <p/>
 * This class delegates most of its implementation to the {@link Locks} utility class.
 *
 * @author Niall Gallagher
 */
public class CompositeLock implements Lock {

	final Deque<Lock> locks;

	public CompositeLock(Lock... locks) {
		this(new LinkedList<Lock>(Arrays.asList(locks)));
	}

	public CompositeLock(Deque<Lock> locks) {
		this.locks = locks;
	}

	@Override
	public void lock() {
		Locks.lockAll(locks);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		Locks.lockInterruptiblyAll(locks);
	}

	@Override
	public boolean tryLock() {
		return Locks.tryLockAll(locks);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return Locks.tryLockAll(time, unit, locks);
	}

	@Override
	public void unlock() {
		// Unlock in reverse order...
		Locks.unlockAll(new Iterable<Lock>() {
			@Override
			public Iterator<Lock> iterator() {
				return locks.descendingIterator();
			}
		});
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException("This lock does not support conditions");
	}
}
