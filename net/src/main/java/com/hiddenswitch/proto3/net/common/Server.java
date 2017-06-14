package com.hiddenswitch.proto3.net.common;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.client.models.Emote;
import com.hiddenswitch.proto3.net.impl.server.ClientConnectionHandler;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

/**
 * An interface that specifies a server instance that's capable of processing {@link Client} actions.
 */
public interface Server extends ClientConnectionHandler {
	@Suspendable
	void onActionReceived(String id, int actionIndex);

	@Suspendable
	void onActionReceived(String id, GameAction action);

	void onMulliganReceived(String id, Player player, List<Card> discardedCards);

	void onMulliganReceived(String messageId2, List<Integer> discardedCardIndices);

	void onEmote(int entityId, Emote.MessageEnum message);

	Client getClient1();

	Client getClient2();
}
