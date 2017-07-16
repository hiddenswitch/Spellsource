package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.models.*;

/**
 * A service that processes bot actions, mulligans and conveniently creates bot games.
 */
public interface Bots {
	/**
	 * Decide which cards to mulligan given a starting hand.
	 * @param request A request containing the cards to choose from.
	 * @return A response that specifies which cards to mulligan.
	 */
	@Suspendable
	MulliganResponse mulligan(MulliganRequest request);

	/**
	 * Decides which action to perform given a list of possibilities and the current game state.
	 * @param request The game state and options for an action.
	 * @return The selected action.
	 */
	@Suspendable
	RequestActionResponse requestAction(RequestActionRequest request);

	/**
	 * Starts a game against a bot.
	 * @param request A request containing the user's ID and chosen deck.
	 * @return Connection and bot information about the new game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	BotsStartGameResponse startGame(BotsStartGameRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Handles a notification that a game has ended for a bot. Typically this returns a bot back to a pool of possible
	 * bot opponents.
	 * @param request The information about the game that is over.
	 * @return Any additional information about the bot's handling of a game over notification.
	 */
	@Suspendable
	NotifyGameOverResponse notifyGameOver(NotifyGameOverRequest request);
}
