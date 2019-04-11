package com.hiddenswitch.spellsource.concurrent;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
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
