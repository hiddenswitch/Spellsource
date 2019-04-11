package com.hiddenswitch.spellsource.concurrent.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.ISemaphore;
import com.hiddenswitch.spellsource.util.Hazelcast;
import com.hiddenswitch.spellsource.concurrent.SuspendableSemaphore;
import io.vertx.core.Vertx;

import java.util.concurrent.TimeUnit;

import static com.hiddenswitch.spellsource.util.Sync.*;
import static io.vertx.ext.sync.Sync.awaitResult;

public class SuspendableHazelcastSemaphore implements SuspendableSemaphore {

	private final String name;
	private final int permits;
	private ISemaphore sem;

	public SuspendableHazelcastSemaphore(String name, int permits) {
		this.name = name;
		this.permits = permits;
	}

	@Override
	@Suspendable
	public boolean tryAcquire(long timeout) throws InterruptedException {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(fut -> {
			try {
				fut.complete(sem.tryAcquire(timeout, TimeUnit.MILLISECONDS));
			} catch (InterruptedException e) {
				fut.complete(false);
			}
		},false, h));
	}

	@Override
	@Suspendable
	public void release() {
		// This could be false if the lock was never obtained.
		invoke0(sem::release);
	}

	@Suspendable
	public void init() throws SuspendExecution {
		Void t = awaitResult(h -> Vertx.currentContext().executeBlocking(fut -> {
			sem = Hazelcast.getHazelcastInstance().getSemaphore(name);
			this.sem.init(permits);
			fut.complete();
		},false, h));
	}
}
