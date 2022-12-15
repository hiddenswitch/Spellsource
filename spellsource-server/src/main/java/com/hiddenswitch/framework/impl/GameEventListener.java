package com.hiddenswitch.framework.impl;

import com.hiddenswitch.spellsource.common.GameState;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.logic.TurnState;

/**
 * An interface specifying server-to-client data updates.
 */
public interface GameEventListener {
	/**
	 * Sends a notification, like touching a card, to the client.
	 *
	 * @param event
	 * @param gameState
	 */
	void sendNotification(Notification event, GameState gameState);

	/**
	 * Notifies the client the game is over
	 *
	 * @param gameState
	 * @param winner
	 */
	void sendGameOver(GameState gameState, Player winner);

	/**
	 * Notifies the client of the current active player
	 *
	 * @param activePlayer
	 */
	void onConnectionStarted(Player activePlayer);

	/**
	 * Notifies the turn has ended and the given player is now active
	 *
	 * @param activePlayer
	 * @param turnNumber
	 * @param turnState
	 */
	void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState);

	/**
	 * Updates the player with this game state
	 *
	 * @param state
	 */
	void onUpdate(GameState state);

	/**
	 * Sends the client an emote
	 *  @param entityId
	 * @param emote
	 */
	void sendEmote(int entityId, String emote);
}
