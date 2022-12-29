package com.hiddenswitch.framework.impl;

import com.hiddenswitch.spellsource.common.GameState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

/**
 * Represents a handler that should listen for game requests.
 */
public interface ActionListener {
	/**
	 * Elapses all awaiting requests, typically due to a timer running out.
	 */
	void elapseAwaitingRequests();

	/**
	 * Requests a game action.
	 *
	 * @param messageId The message ID
	 * @param state     The game state
	 * @param actions   The possible actions
	 */
	void onRequestAction(String messageId, GameState state, List<GameAction> actions);

	/**
	 * Requests a mulligan
	 *
	 * @param messageId The message ID
	 * @param state     The game state
	 * @param cards     The cards that can be discarded
	 * @param playerId  The player doing the discards
	 */
	void onMulligan(String messageId, GameState state, List<Card> cards, int playerId);
}
