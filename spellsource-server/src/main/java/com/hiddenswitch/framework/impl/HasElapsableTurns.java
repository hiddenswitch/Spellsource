package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.Suspendable;

/**
 * Indicates that this behvaiour can process elapsed turns
 */
public interface HasElapsableTurns {
	/**
	 * When {@code true}, indicates that the behaviour has elapsed its time
	 *
	 * @return
	 */
	boolean isElapsed();

	/**
	 * Clears or sets whether or not the behaviour's turn has elapsed.
	 *
	 * @param elapsed
	 * @return
	 */
	@Suspendable
	HasElapsableTurns setElapsed(boolean elapsed);

	/**
	 * Elapses all awaiting requests.
	 */
	@Suspendable
	void elapseAwaitingRequests();
}
