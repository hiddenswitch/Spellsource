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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Extends the JDK {@link ReadWriteLock}, providing an <b>update lock</b> in addition to the read lock and the write
 * lock.
 * <p/>
 * The {@link #updateLock update lock} supports read-only operations and can coexist with multiple
 * {@link #readLock read lock}s held simultaneously by other reader threads. However it may also be upgraded from
 * its read-only status to a {@link #writeLock write lock}, and it may be downgraded again back to a read lock.
 * <p/>
 * See implementation {@link ReentrantReadWriteUpdateLock} for more details.
 *
 * @author Niall Gallagher
 */
public interface ReadWriteUpdateLock extends ReadWriteLock {

	/**
	 * Returns a lock which allows reading and which may also be upgraded to a lock allowing writing.
	 *
	 * @return a lock which allows reading and which may also be upgraded to a lock allowing writing.
	 */
	Lock updateLock();
}
