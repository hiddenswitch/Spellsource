package com.hiddenswitch.framework.impl;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class NoOpCondition implements Condition {
	@Override
	public void await() throws InterruptedException {

	}

	@Override
	public void awaitUninterruptibly() {

	}

	@Override
	public long awaitNanos(long nanosTimeout) throws InterruptedException {
		return nanosTimeout;
	}

	@Override
	public boolean await(long time, TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public boolean awaitUntil(@NotNull Date deadline) throws InterruptedException {
		return true;
	}

	@Override
	public void signal() {
	}

	@Override
	public void signalAll() {
	}
}
