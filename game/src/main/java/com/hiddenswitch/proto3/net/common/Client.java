package com.hiddenswitch.proto3.net.common;

import java.util.List;

import com.hiddenswitch.proto3.net.client.models.Emote;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.Notification;

/**
 * An interface that specifies the boundary between a {@link net.demilich.metastone.game.GameContext} and a networking
 * channel like a websocket or a plain TCP socket.
 */
public interface Client {
	void onNotification(Notification event, GameState gameState);

	void onGameEnd(Player winner);

	void setPlayers(Player localPlayer, Player remotePlayer);

	void onActivePlayer(Player activePlayer);

	void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState);

	void onUpdate(GameState state);

	void onRequestAction(String messageId, GameState state, List<GameAction> actions);

	void onMulligan(String messageId, GameState state, List<Card> cards, int playerId);

	/**
	 * Send an emote to a client. By default, there's no implementation except for Unity clients.
	 * @param entityId The entity from which the emote should originate. Typically a hero entity.
	 * @param emote The emote to send
	 */
	default void onEmote(int entityId, Emote.MessageEnum emote) {
	};

	void close();

	/**
	 * Gets an object that represents the underlying networking socket that powers this client. This is helpful for
	 * keeping track of reconnecting users, who may be the same {@link Client} but connected with different sockets.
	 *
	 * @return An object whose {@link Object#hashCode()} is valid for {@link java.util.Map} keys, to help the server
	 * infrastructure keep track of which sockets correspond to which {@link Client} objects.
	 */
	Object getPrivateSocket();

	/**
	 * Called when the last event in a stack of {@link GameEvent} has been evaluated. Typically, a client should be
	 * notified of an entire sequence of events, so that it has valid data at the end of each event, rather than
	 * as the events are processed.
	 */
	void lastEvent();
}