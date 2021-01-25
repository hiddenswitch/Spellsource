package com.hiddenswitch.spellsource.draft;

/**
 * Indicates different states of a draft.
 *
 * @see DraftContext to advance a draft using the appropriate methods.
 */
public enum DraftStatus {
	/**
	 * The draft is currently accepting choices for cards.
	 */
	IN_PROGRESS,
	/**
	 * The draft expects the user to select a champion.
	 */
	SELECT_HERO,
	/**
	 * The draft has not yet started (default state).
	 */
	NOT_STARTED,
	/**
	 * The draft is complete and the user can enter a queue with the deck built using the draft.
	 * <p>
	 * Retrieve the deck using {@code draftContext.getPublicState().createDeck()} or retrieve the deck by its ID using
	 * {@code draftContext.getPublicState().getDeckId()}.
	 */
	COMPLETE,
	/**
	 * The draft has been retired (ended) by the user.
	 */
	RETIRED
}
