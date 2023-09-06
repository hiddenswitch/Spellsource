package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.Heuristic;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GreedyOptimizeTurn extends IntelligentBehaviour {

	private final Logger logger = LoggerFactory.getLogger(GreedyOptimizeTurn.class);

	private final Heuristic heuristic;

	private int assignedGC;
	private final HashMap<ActionType, Integer> evaluatedActions = new LinkedHashMap<ActionType, Integer>();
	private final TranspositionTable table = new TranspositionTable();

	public GreedyOptimizeTurn(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	private double alphaBeta(GameContext context, int playerId, GameAction action, int depth) {
		GameContext simulation = context.clone();
		simulation.performAction(playerId, action);
		if (!evaluatedActions.containsKey(action.getActionType())) {
			evaluatedActions.put(action.getActionType(), 0);
		}
		evaluatedActions.put(action.getActionType(), evaluatedActions.get(action.getActionType()) + 1);
		if (depth == 0 || simulation.getActivePlayerId() != playerId || simulation.updateAndGetGameOver()) {
			return heuristic.getScore(simulation, playerId);
		}

		List<GameAction> validActions = simulation.getValidActions();

		double score = Double.NEGATIVE_INFINITY;
		if (table.known(simulation)) {
			return table.getScore(simulation);
			// logger.info("GameState is known, has score of {}", score);
		} else {
			for (GameAction gameAction : validActions) {
				score = Math.max(score, alphaBeta(simulation, playerId, gameAction, depth - 1));
				if (score >= 10000) {
					break;
				}
			}
			table.save(simulation, score);
		}

		return score;
	}

	@Override
	public GreedyOptimizeTurn clone() {
		try {
			return new GreedyOptimizeTurn(heuristic.getClass().getConstructor().newInstance());
		} catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	@Override
	public String getName() {
		return "Min-Max Turn";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() >= 4) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		// for now, do now evaluate battecry actions
		if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
			return validActions.get(context.getLogic().random(validActions.size()));
		}

		if (assignedGC != 0 && assignedGC != context.hashCode()) {
			logger.warn("AI behaviour was used in another context!");
		}

		assignedGC = context.hashCode();
		evaluatedActions.clear();
		table.clear();

		GameAction bestAction = validActions.get(0);
		double bestScore = Double.NEGATIVE_INFINITY;

		for (GameAction gameAction : validActions) {
			logger.debug("********************* SIMULATION STARTS *********************");
			double score = alphaBeta(context, player.getId(), gameAction, 3);
			if (score > bestScore) {
				bestAction = gameAction;
				bestScore = score;
			}
			logger.debug("********************* SIMULATION ENDS, Action {} achieves score {}", gameAction, score);
		}

		int totalActionCount = 0;
		for (ActionType actionType : evaluatedActions.keySet()) {
			int count = evaluatedActions.get(actionType);
			logger.debug("{} actions of type {} have been evaluated this turn", count, actionType);
			totalActionCount += count;
		}
		logger.debug("{} actions in total have been evaluated this turn", totalActionCount);
		logger.debug("Selecting best action {} with score {}", bestAction, bestScore);

		return bestAction;
	}

	/*private double simulateAction(GameContext context, int playerId, GameAction action) {
		GameContext simulation = context.clone();
		simulation.performAction(playerId, action);
		if (!evaluatedActions.containsKey(action.getActionType())) {
			evaluatedActions.put(action.getActionType(), 0);
		}
		evaluatedActions.put(action.getActionType(), evaluatedActions.get(action.getActionType()) + 1);
		if (simulation.getActivePlayerId() != playerId || simulation.gameDecided()) {
			return heuristic.getScore(simulation, playerId);
		}
		List<GameAction> validActions = simulation.getValidActions();
		if (validActions.size() == 0) {
			throw new RuntimeException("No more possible moves, last action was: " + action);
		}
		double bestScore = Integer.MIN_VALUE;
		for (GameAction gameAction : validActions) {
			bestScore = Math.max(bestScore, simulateAction(simulation, playerId, gameAction));
		}
		return bestScore;
	}*/

}
