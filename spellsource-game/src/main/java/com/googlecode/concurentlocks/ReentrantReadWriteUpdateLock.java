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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/**
 * An implementation of {@link ReadWriteUpdateLock}, extending the functionality of the JDK
 * {@link ReentrantReadWriteLock} with an <b>update lock</b> in addition to the read and write lock, supporting upgrade
 * from read-only operation to writing status, and downgrade again.
 *
 * <h1>Background - JDK ReentrantReadWriteLock</h1>
 * The <tt>ReentrantReadWriteLock</tt> in the JDK classifies threads as needing to <i>read-without-write</i>,
 * <i>write-without-read</i>, or <i>write-before-read</i>. Threads can obtain the read lock, or the write lock, and if
 * they have the write lock, they can downgrade it to a read lock. But the key limitation is that it does not support
 * <i>read-before-write</i>: threads which hold a read lock cannot upgrade it to a write lock.
 * <p/>
 * Imagine a data structure, let's say a cache, where most requests are to read data, but also there is a maintenance
 * thread responsible for periodically traversing the data structure to find and update stale entries. The maintenance
 * thread thus would mostly read entries while traversing the data structure, and periodically it might encounter
 * entries which need to be updated.
 * <p/>
 * If a classic ReentrantReadWriteLock is used to control access, the <i>write-before-read</i> support afforded by that lock
 * would be <i>insufficient</i>, because if at any point the thread updated an entry and then downgraded its write lock to a
 * read lock, it would not be able to upgrade it again to a write lock if it found other stale entries. So the
 * maintenance thread would need to hold the write lock for its entire traversal of the data structure, preventing read
 * access for that entire duration.
 *
 * <h1>ReentrantReadWriteUpdateLock Overview</h1>
 * The third type of lock provided, an <i>update lock</i>, is like a <i>super read lock</i>. It behaves like a read lock, in that it
 * allows read access to the thread which holds it, and it concurrently <i>"plays nice"</i> with other threads which hold
 * regular read locks, allowing those threads concurrent read access.
 * <p/>
 * The key difference is that the update lock can be upgraded from its read-only status, to a write lock. Thus it
 * supports <i>read-before-write</i> usage. Also the write lock can be downgraded again to an update lock, <i>write-before-read</i>
 * usage. A restriction however is that, similar to the write lock, only one thread may acquire the update lock at a
 * time.
 * <p/>
 * This is sufficient in situations like the example above, to allow read-mostly, write-occasionally threads to operate
 * on the data structure without blocking access to read-only threads most of the time: upgrading to the write lock only
 * for the short periods in which they need it, before downgrading to the update lock again. As such it can reduce the
 * latency for read-only requests, and increase concurrency in applications which otherwise would use a read-write lock.
 *
 * <h2>Lock Acquisition Paths</h2>
 * <table border="1">
 * <tr><th>Lock type</th><th>Associated Permissions</th><th>Lock acquisition paths</th><th>Lock downgrade paths</th><th>Prevented with exception *</th></tr>
 * <tr><td>Read</td><td>Read (shared)</td><td>None → Read<br/>Read → Read (reentrant)</td><td>Read → None</td><td>Read → Update<br/>Read → Write</td></tr>
 * <tr><td>Update</td><td>Read (shared)</td><td>None → Update<br/>Update → Update (reentrant)<br/>Write → Update (reentrant)</td><td>Update → None</td><td>Update → Read</td></tr>
 * <tr><td>Write</td><td>Read (exclusive)<br/>Write (exclusive)</td><td>None → Write<br/>Update → Write<br/>Write → Write (reentrant)</td><td>Write → Update<br/>Write → None</td><td>Write → Read</td></tr>
 * </table>
 * * An <tt>IllegalStateException</tt> will be thrown if a thread holding a regular read lock tries to acquire the
 * update or write lock, or if a thread holding the update or write lock tries to acquire a regular read lock.
 *
 * @author Niall Gallagher
 */
public class ReentrantReadWriteUpdateLock implements ReadWriteUpdateLock {

	final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	final Lock updateMutex = new ReentrantLock();

	final ReadLock readLock = new ReadLock();
	final UpdateLock updateLock = new UpdateLock();
	final WriteLock writeLock = new WriteLock();

	@Override
	public Lock updateLock() {
		return updateLock;
	}

	@Override
	public Lock readLock() {
		return readLock;
	}

	@Override
	public Lock writeLock() {
		return writeLock;
	}

	static abstract class HoldCountLock implements Lock {

		static class HoldCount {
			int value;
		}

		final ThreadLocal<HoldCount> threadHoldCount = new ThreadLocal<HoldCount>() {
			@Override
			protected HoldCount initialValue() {
				return new HoldCount();
			}
		};

		final Lock backingLock;

		public HoldCountLock(Lock backingLock) {
			this.backingLock = backingLock;
		}

		HoldCount holdCount() {
			return threadHoldCount.get();
		}

		@Override
		public void lock() {
			validatePreconditions();
			backingLock.lock();
			holdCount().value++;
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			validatePreconditions();
			backingLock.lockInterruptibly();
			holdCount().value++;
		}

		@Override
		public boolean tryLock() {
			validatePreconditions();
			if (backingLock.tryLock()) {
				holdCount().value++;
				return true;
			}
			return false;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			validatePreconditions();
			if (backingLock.tryLock(time, unit)) {
				holdCount().value++;
				return true;
			}
			return false;
		}

		@Override
		public void unlock() {
			backingLock.unlock();
			holdCount().value--;
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("This lock does not support conditions");
		}

		abstract void validatePreconditions();
	}

	class ReadLock extends HoldCountLock {

		public ReadLock() {
			super(readWriteLock.readLock());
		}

		void validatePreconditions() {
			if (updateLock.holdCount().value > 0) {
				throw new IllegalStateException("Cannot acquire read lock, as this thread previously acquired and must first release the update lock");
			}
		}
	}

	class UpdateLock extends HoldCountLock {

		public UpdateLock() {
			super(updateMutex);
		}

		void validatePreconditions() {
			if (readLock.holdCount().value > 0) {
				throw new IllegalStateException("Cannot acquire update lock, as this thread previously acquired and must first release the read lock");
			}
		}
	}

	class WriteLock implements Lock {

		@Override
		public void lock() {
			validatePreconditions();
			// Acquire UPDATE lock again, even if calling thread might already hold it.
			// This allow threads to go from both NONE -> WRITE and from UPDATE -> WRITE.
			// This also ensures that only the thread holding the single UPDATE lock,
			// can request the WRITE lock...
			Locks.lockAll(updateLock, readWriteLock.writeLock());
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			validatePreconditions();
			Locks.lockInterruptiblyAll(updateLock, readWriteLock.writeLock());
		}

		@Override
		public boolean tryLock() {
			validatePreconditions();
			return Locks.tryLockAll(updateLock, readWriteLock.writeLock());
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			validatePreconditions();
			return Locks.tryLockAll(time, unit, updateLock, readWriteLock.writeLock());
		}

		@Override
		public void unlock() {
			Locks.unlockAll(readWriteLock.writeLock(), updateLock);
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("This lock does not support conditions");
		}

		void validatePreconditions() {
			if (readLock.holdCount().value > 0) {
				throw new IllegalStateException("Cannot acquire write lock, as this thread previously acquired and must first release the read lock");
			}
		}
	}
}
