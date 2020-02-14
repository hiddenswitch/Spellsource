package com.hiddenswitch.spellsource.draft;

/**
 * Indicates the user made an error, typically an invalid or out-of-order choice, during drafting.
 */
public abstract class DraftException extends IndexOutOfBoundsException {
	DraftException() {
		super();
	}

	DraftException(int choiceIndex) {
		super(Integer.toString(choiceIndex));
	}
}
