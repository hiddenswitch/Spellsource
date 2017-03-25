package com.hiddenswitch.proto3.net.common;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

public interface Server {
	@Suspendable
	void onActionReceived(String id, GameAction action);
	
	void onMulliganReceived(String id, Player player, List<Card> discardedCards);
}
