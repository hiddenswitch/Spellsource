package com.hiddenswitch.framework.impl;

import com.hiddenswitch.spellsource.common.GameState;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.logic.TurnState;

import java.util.List;

/**
 * An interface that specifies the boundary between a {@link net.demilich.metastone.game.GameContext} and a
 * networked player verticle.
 */
public interface Client extends Behaviour, Verticle {
	boolean isElapsed();

	Client setElapsed(boolean elapsed);

	void elapseAwaitingRequests();

	void onRequestAction(String messageId, GameState state, List<GameAction> actions);

	void onMulligan(String messageId, GameState state, List<Card> cards, int playerId);

	void sendNotification(Notification event, GameState gameState);

	void onConnectionStarted(Player activePlayer);

	void onUpdate(GameState state);

	void sendEmote(int entityId, String emote);

	void lastEvent();

	int getPlayerId();

	String getUserId();

	void closeInboundMessages();

	Future<Void> unregisterConsumer();

	Future<Void> lastWriteFuture();
}
