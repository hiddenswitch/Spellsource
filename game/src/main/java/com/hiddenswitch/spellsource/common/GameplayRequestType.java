package com.hiddenswitch.spellsource.common;

/**
 * Represents the kind of gameplay request a client can take.
 */
public enum GameplayRequestType {
	/**
	 * No request specified.
	 */
	NONE,
	/**
	 * A mulligan request.
	 */
	MULLIGAN,
	/**
	 * A game action.
	 */
	ACTION
}
