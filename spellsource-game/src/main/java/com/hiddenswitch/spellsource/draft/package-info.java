/**
 * Contains code that implements the drafting mechanic in Spellsource.
 * <p>
 * Drafting is the process of building a deck from a series of irreversible choices of cards. A typical draft starts
 * with select a champion, followed by choosing one of three cards randomly drawn from the collection until a {@link
 * net.demilich.metastone.game.logic.GameLogic#MAX_DECK_SIZE} deck is built. This process is implemented in the {@link
 * com.hiddenswitch.spellsource.draft.DraftLogic}, which delegates the choices to {@link
 * com.hiddenswitch.spellsource.draft.DraftBehaviour}.
 * <p>
 * {@link com.hiddenswitch.spellsource.draft.PrivateDraftState} contains the cards that are going to be shown to the
 * user and should remain secret, to prevent the user from cheating. The {@link com.hiddenswitch.spellsource.draft.PublicDraftState}
 * contains the current state of the draft.
 * <p>
 * The {@code net} package contains code that actually sends requests to a networked Unity client and maintains the
 * state of the draft over the user's lifetime.
 */
package com.hiddenswitch.spellsource.draft;