package com.hiddenswitch.spellsource.concurrent;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.concurrent.impl.SuspendableHazelcastSemaphore;

public interface SuspendableSemaphore {

	@Suspendable
	static SuspendableSemaphore create(String name, int permits) throws SuspendExecution {
		SuspendableHazelcastSemaphore semaphore = new SuspendableHazelcastSemaphore(name, permits);
		semaphore.init();
		return semaphore;
	}

	@Suspendable
	boolean tryAcquire(long timeout) throws InterruptedException;

	@Suspendable
	void release();
}
