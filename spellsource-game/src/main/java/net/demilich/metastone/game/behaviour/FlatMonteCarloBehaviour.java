package net.demilich.metastone.game.behaviour;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A behaviour which randomly samples the game tree seeking sequences of actions that terminate in wins.
 */
public class FlatMonteCarloBehaviour extends IntelligentBehaviour {
	private static class ScoredAction {
		protected GameAction action;
		protected double score;
	}

	private final static Logger logger = LoggerFactory.getLogger(FlatMonteCarloBehaviour.class);
	private int iterations;
	private long timeout = 59000;

	public FlatMonteCarloBehaviour() {
		this(8);
	}

	public FlatMonteCarloBehaviour(int iterations) {
		this.iterations = iterations;
	}

	@Suspendable
	private GameAction getBestAction(Map<GameAction, Double> actionScores) {
		GameAction bestAction = null;
		double bestScore = Integer.MIN_VALUE;
		for (GameAction actionEntry : actionScores.keySet()) {
			double score = actionScores.get(actionEntry);
			if (score > bestScore) {
				bestAction = actionEntry;
				bestScore = score;
			}
		}
		logger.debug("getBestAction: Best action determined by MonteCarlo: {}", bestAction);
		return bestAction;
	}

	@Override
	public String getName() {
		return "Flat Monte-Carlo " + iterations;
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() >= 4) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Suspendable
	private int playRandomUntilEnd(GameContext simulation, int playerId) {
		simulation.setBehaviours(new Behaviour[]{new PlayRandomBehaviour(), new PlayRandomBehaviour()});
		simulation.resume();
		return simulation.getWinningPlayerId() == playerId ? 1 : 0;
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}
		final int playerId = player.getId();
		final long startMillis = System.currentTimeMillis();
		Map<GameAction, Double> actionScores = validActions
				.parallelStream()
				.map(gameAction -> {
					double score = simulate(context, playerId, gameAction, startMillis);
					ScoredAction scored = new ScoredAction();
					scored.action = gameAction;
					scored.score = score;
					return scored;
				}).collect(Collectors.toMap(scored -> scored.action, scored -> scored.score));

		return getBestAction(actionScores);
	}

	@Suspendable
	private double simulate(GameContext context, int playerId, GameAction action, long startMillis) {
		GameContext simulation = context.clone();
		simulation.performAction(simulation.getActivePlayerId(), action);
		if (simulation.updateAndGetGameOver()) {
			// Action leads to lethal
			return simulation.getWinningPlayerId() == playerId ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		}
		double score = 0;
		int i = 0;
		for (; i < iterations; i++) {
			final boolean timedOut = System.currentTimeMillis() - startMillis > getTimeout();
			if (timedOut) {
				break;
			}
			score += playRandomUntilEnd(simulation.clone(), playerId);
		}
		return score;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
