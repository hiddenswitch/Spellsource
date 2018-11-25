package com.hiddenswitch.spellsource.util;

import com.github.fromage.quasi.strands.concurrent.ReentrantLock;

public class NoOpLock extends ReentrantLock {
	@Override
	public void lock() {
		return;
	}

	@Override
	public void unlock() {
		return;
	}
}
