package com.hiddenswitch.framework.impl;

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
	HasElapsableTurns setElapsed(boolean elapsed);

	/**
	 * Elapses all awaiting requests.
	 */
	void elapseAwaitingRequests();
}
