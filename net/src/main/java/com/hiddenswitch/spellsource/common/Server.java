package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Emote;
import com.hiddenswitch.spellsource.impl.server.ClientConnectionHandler;

import java.util.Random;

/**
 * An interface that specifies a server instance that's capable of processing {@link Client} actions.
 */
public interface Server extends ClientConnectionHandler {

	@Suspendable
	void onEmote(Client sender, int entityId, Emote.MessageEnum message);

	@Suspendable
	void onConcede(Client sender);

	@Suspendable
	void onTouch(Client sender, int entityId);

	@Suspendable
	void onUntouch(Client sender, int entityId);

	/**
	 * Have both players connected?
	 *
	 * @return {@code true} if both players have sent their first messages.
	 * to the game session.
	 */
	boolean isGameReady();

	/**
	 * Gets the server's random instance
	 * @return
	 */
	Random getRandom();

	String getGameId();
}
