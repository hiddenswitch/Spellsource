package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

public interface ActionListener {
	@Suspendable
	void elapseAwaitingRequests();

	@Suspendable
	void onRequestAction(String messageId, GameState state, List<GameAction> actions);

	@Suspendable
	void onMulligan(String messageId, GameState state, List<Card> cards, int playerId);
}
