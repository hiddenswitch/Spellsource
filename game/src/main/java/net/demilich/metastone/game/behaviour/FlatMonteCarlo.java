package net.demilich.metastone.game.behaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import co.paralleluniverse.fibers.Suspendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

public class FlatMonteCarlo extends AbstractBehaviour {
	private final static Logger logger = LoggerFactory.getLogger(FlatMonteCarlo.class);
	private int iterations;
	private long timeout = 59000;

	public FlatMonteCarlo(int iterations) {
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
		logger.debug("Best action determined by MonteCarlo: " + bestAction.getActionType());
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
		for (Player player : simulation.getPlayers()) {
			player.setBehaviour(new PlayRandomBehaviour());
		}
		simulation.resume();
		return simulation.getWinningPlayerId() == playerId ? 1 : 0;
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			return validActions.get(0);
		}
		final long startMillis = System.currentTimeMillis();
		Map<GameAction, Double> actionScores = validActions.parallelStream().collect(Collectors.toMap(
				Function.identity(), gameAction -> simulate(context, player.getId(), gameAction, startMillis)
		));

		return getBestAction(actionScores);
	}

	@Suspendable
	private double simulate(GameContext context, int playerId, GameAction action, long startMillis) {
		GameContext simulation = context.clone();
		simulation.getLogic().performGameAction(simulation.getActivePlayerId(), action);
		if (simulation.updateAndGetGameOver()) {
			return simulation.getWinningPlayerId() == playerId ? 1 : 0;
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
		return score / i;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
