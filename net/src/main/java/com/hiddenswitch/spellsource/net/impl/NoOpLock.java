package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.strands.concurrent.ReentrantLock;

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
