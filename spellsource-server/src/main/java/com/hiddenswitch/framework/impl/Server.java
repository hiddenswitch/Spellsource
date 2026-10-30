package com.hiddenswitch.framework.impl;

import java.util.Random;

/**
 * An interface that specifies a server instance that's capable of processing {@link Client} actions.
 */
public interface Server {
	void onPlayerReady(Client client);

	void onPlayerReconnected(Client client);

	void onEmote(Client sender, int entityId, String message);

	void onConcede(Client sender);

	void onTouch(Client sender, int entityId);

	void onUntouch(Client sender, int entityId);

	/**
	 * Have both players connected?
	 *
	 * @return {@code true} if both players have sent their first messages to the game session.
	 */
	boolean isGameReady();

	/**
	 * Gets the server's random instance.
	 */
	Random getRandom();

	/**
	 * Get this game's ID, or some fixed string if this is a local game.
	 */
	String getGameId();

	/**
	 * Returns the number of milliseconds left in this turn, or {@code null} if there is no limit.
	 */
	Long getMillisRemaining();

	/**
	 * Checks if the game is over.
	 */
	boolean isGameOver();
}
