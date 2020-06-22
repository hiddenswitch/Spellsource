package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface NoArgs {
	@Suspendable
	void apply();
}
