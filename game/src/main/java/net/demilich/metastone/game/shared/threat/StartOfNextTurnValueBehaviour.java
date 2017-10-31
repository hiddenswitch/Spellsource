package net.demilich.metastone.game.shared.threat;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;

import java.util.List;

public class StartOfNextTurnValueBehaviour extends GameStateValueBehaviour {
	public StartOfNextTurnValueBehaviour() {
		super(FeatureVector.getFittest(), "Botty McBotface");
		this.timeout = 55000;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		long startMillis = System.currentTimeMillis();
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		int depth = 30;

		GameAction bestAction = validActions.get(0);
		double bestScore = Double.NEGATIVE_INFINITY;

		for (GameAction gameAction : validActions) {
			double score = alphaBeta(context, player.getId(), gameAction, depth, startMillis, context.getTurn() + 2);
			if (score > bestScore) {
				bestAction = gameAction;
				bestScore = score;
			}
		}

		return bestAction;
	}

	@Suspendable
	private double alphaBeta(GameContext context, int playerId, GameAction action, int depth, long startMillis, int terminateAtStartOfTurn) {
		GameContext simulation = getClone(context);
		double score = Float.NEGATIVE_INFINITY;
		final boolean timedOut = System.currentTimeMillis() - startMillis > timeout;

		if (simulation.isDisposed()) {
			return Float.NEGATIVE_INFINITY;
		}

		simulation.getLogic().performGameAction(playerId, action);

		if (timedOut
				|| depth == 0
				|| simulation.updateAndGetGameOver()) {
			return heuristic.getScore(simulation, playerId);
		}

		// Did you end the turn?
		if (context.getTurnState() == TurnState.TURN_ENDED) {
			simulation.startTurn(playerId == 0 ? 1 : 0);
			// Let the opponent evaluate as though it was a game state value behaviour until that turn is over
			while (simulation.takeActionInTurn()) {
			}

			simulation.startTurn(playerId);
		}

		// Now, if we're starting the turn
		if (terminateAtStartOfTurn == simulation.getTurn()) {
			return heuristic.getScore(simulation, playerId);
		}

		List<GameAction> validActions = simulation.getValidActions();
		for (GameAction gameAction : validActions) {
			score = Math.max(score, alphaBeta(simulation, playerId, gameAction, depth - 1, startMillis, terminateAtStartOfTurn));
		}

		return score;
	}


}
