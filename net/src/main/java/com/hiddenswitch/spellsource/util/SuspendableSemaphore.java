package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;

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
