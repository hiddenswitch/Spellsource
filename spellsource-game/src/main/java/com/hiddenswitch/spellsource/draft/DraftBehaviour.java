package com.hiddenswitch.spellsource.draft;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Describes a behaviour during a draft. This helps implementing drafting mechanics in Spellsource and is implemented by
 * the appropriate networking code to proxy a user in the Unity client.
 *
 * @see DraftLogic for more on the rules of drafting.
 */
public interface DraftBehaviour {
	/**
	 * Given a list of champions, which champion will this draft be?
	 *
	 * @param classes Some number of champions.
	 * @param result  Called with the chosen champion.
	 */
	void chooseHeroAsync(List<String> classes, Handler<AsyncResult<String>> result);

	/**
	 * Given a list of cards, choose one card. This card is typically added to the deck.
	 *
	 * @param cards             A list of choices currently.
	 * @param selectedCardIndex Called with the index of the choice.
	 */
	void chooseCardAsync(List<String> cards, Handler<AsyncResult<Integer>> selectedCardIndex);

	/**
	 * Notifies the behaviour of the current public draft state as soon as it changes.
	 *
	 * @param state The state
	 */
	void notifyDraftState(PublicDraftState state);

	/**
	 * Notifies the behaviour of the current public draft state as soon as it changes, and receives a callback when the
	 * client has acknowledged receipt of the data.
	 *
	 * @param state        The state
	 * @param acknowledged Called when the state was received (written to the network socket in practice).
	 */
	void notifyDraftStateAsync(PublicDraftState state, Handler<AsyncResult<Void>> acknowledged);
}
