package com.hiddenswitch.spellsource.draft;

/**
 * Indicates the end user made an invalid card selection (the index was out of bounds).
 */
public final class InvalidDraftCardSelectionException extends DraftException {
	private final int choiceIndex;
	private final PrivateDraftState privateState;

	/**
	 * Creates this exception
	 *
	 * @param choiceIndex
	 * @param privateState
	 */
	public InvalidDraftCardSelectionException(int choiceIndex, PrivateDraftState privateState) {
		super(choiceIndex);
		this.choiceIndex = choiceIndex;
		this.privateState = privateState;
	}

	/**
	 * The invalid choice index that was made
	 *
	 * @return
	 */
	public int getChoiceIndex() {
		return choiceIndex;
	}

	/**
	 * The current private state of the draft
	 *
	 * @return
	 */
	public PrivateDraftState getPrivateState() {
		return privateState;
	}
}
