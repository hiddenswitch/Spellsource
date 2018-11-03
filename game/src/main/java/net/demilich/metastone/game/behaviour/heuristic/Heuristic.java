package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.GameContext;

/**
 * A heuristic is a function that takes a game state and returns a score from the given player's point of view.
 */
@FunctionalInterface
public interface Heuristic {

	/**
	 * A scoring function mapping from game state to a double representing the strength of that game state.
	 *
	 * @param context  The game context. Use its {@link GameContext#getGameState()} to evaluate its game state, do not
	 *                 mutate it here.
	 * @param playerId The player whose point of view should be used to calculate this score.
	 * @return A possibly negative score.
	 */
	double getScore(GameContext context, int playerId);
}
