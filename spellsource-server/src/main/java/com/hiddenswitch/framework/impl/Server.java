package com.hiddenswitch.framework.impl;

import io.opentracing.SpanContext;

import java.util.Random;

/**
 * An interface that specifies a server instance that's capable of processing {@link Client} actions.
 */
public interface Server extends ClientConnectionHandler {

	void onEmote(Client sender, int entityId, String message);

	void onConcede(Client sender);

	void onTouch(Client sender, int entityId);

	void onUntouch(Client sender, int entityId);

	/**
	 * Have both players connected?
	 *
	 * @return {@code true} if both players have sent their first messages. to the game session.
	 */
	boolean isGameReady();

	/**
	 * Gets the server's random instance
	 *
	 * @return
	 */
	Random getRandom();

	/**
	 * Get this game's ID, or some fixed string if this is a local game.
	 *
	 * @return A game ID.
	 */
	String getGameId();

	/**
	 * Returns the number of milliseconds left in this turn, or {@code null} if there is no limit.
	 *
	 * @return
	 */
	Long getMillisRemaining();

	/**
	 * Checks if the game is over.
	 *
	 * @return {@code true} if the game is over.
	 */
	boolean isGameOver();
}
