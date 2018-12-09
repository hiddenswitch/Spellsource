package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Suspendable;

public interface HasElapsableTurns {
	boolean isElapsed();

	HasElapsableTurns setElapsed(boolean elapsed);

	@Suspendable
	void elapseAwaitingRequests();
}
