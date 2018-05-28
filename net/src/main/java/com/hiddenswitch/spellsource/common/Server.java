package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Emote;
import com.hiddenswitch.spellsource.impl.server.ClientConnectionHandler;
import net.demilich.metastone.game.actions.GameAction;

import java.util.List;

/**
 * An interface that specifies a server instance that's capable of processing {@link Writer} actions.
 */
public interface Server extends ClientConnectionHandler {
	@Suspendable
	void onActionReceived(String id, int actionIndex);

	@Suspendable
	void onActionReceived(String id, GameAction action);

	@Suspendable
	void onMulliganReceived(String messageId2, List<Integer> discardedCardIndices);

	@Suspendable
	void onEmote(int entityId, Emote.MessageEnum message);

	@Suspendable
	void onConcede(int playerId);

	@Suspendable
	void onTouch(int playerId, int entityId);

	@Suspendable
	void onUntouch(int playerId, int entityId);

	Writer getClient1();

	Writer getClient2();
}
