package com.hiddenswitch.spellsource.common;

import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Emote;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.utils.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.Notification;

/**
 * An interface that specifies the boundary between a {@link net.demilich.metastone.game.GameContext} and a networking
 * channel like a websocket or a plain TCP socket.
 */
public interface Writer {
	@Suspendable
	void onNotification(Notification event, GameState gameState);

	@Suspendable
	void onGameEnd(Player winner);

	@Suspendable
	@Deprecated
	void setPlayers(Player localPlayer, Player remotePlayer);

	@Suspendable
	void onActivePlayer(Player activePlayer);

	@Suspendable
	void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState);

	@Suspendable
	void onUpdate(GameState state);

	void onRequestAction(String messageId, GameState state, List<GameAction> actions);

	@Suspendable
	void onMulligan(String messageId, GameState state, List<Card> cards, int playerId);

	/**
	 * Send an emote to a client. By default, there's no implementation except for Unity clients.
	 *
	 * @param entityId The entity from which the emote should originate. Typically a hero entity.
	 * @param emote    The emote to send
	 */
	@Suspendable
	default void onEmote(int entityId, Emote.MessageEnum emote) {
	}

	@Suspendable
	void close();

	/**
	 * Gets an object that represents the underlying networking socket that powers this client. This is helpful for
	 * keeping track of reconnecting users, who may be the same {@link Writer} but connected with different sockets.
	 *
	 * @return An object whose {@link Object#hashCode()} is valid for {@link java.util.Map} keys, to help the server
	 * infrastructure keep track of which sockets correspond to which {@link Writer} objects.
	 */
	@Suspendable
	Object getPrivateSocket();

	/**
	 * Called when the last event in a stack of {@link GameEvent} has been evaluated. Typically, a client should be
	 * notified of an entire sequence of events, so that it has valid data at the end of each event, rather than as the
	 * events are processed.
	 */
	@Suspendable
	void lastEvent();

	static boolean isOpen(Writer writer) {
		return writer != null
				&& writer.isOpen();
	}

	default boolean isOpen() {
		return true;
	}
}