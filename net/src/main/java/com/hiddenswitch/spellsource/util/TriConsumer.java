package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;

@FunctionalInterface
public interface TriConsumer<T1, T2, T3> {

	@Suspendable
	void accept(T1 arg1, T2 arg2, T3 arg3);
}
