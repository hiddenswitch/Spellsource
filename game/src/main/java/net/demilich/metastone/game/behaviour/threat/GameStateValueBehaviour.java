package net.demilich.metastone.game.behaviour.threat;

import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.NotificationProxy;
import net.demilich.metastone.trainingmode.RequestTrainingDataNotification;
import net.demilich.metastone.trainingmode.TrainingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.cards.Card;

public class GameStateValueBehaviour extends AbstractBehaviour {

	private final Logger logger = LoggerFactory.getLogger(GameStateValueBehaviour.class);

	private IGameStateHeuristic heuristic;
	private FeatureVector featureVector;
	private String nameSuffix = "";
	private long timeout = 2000;

	public GameStateValueBehaviour() {
	}

	public GameStateValueBehaviour(FeatureVector featureVector, String nameSuffix) {
		this.featureVector = featureVector;
		this.nameSuffix = nameSuffix;
		this.heuristic = new ThreatBasedHeuristic(featureVector);
	}

	@Suspendable
	private double alphaBeta(GameContext context, int playerId, GameAction action, int depth, long startMillis) {
		GameContext simulation = getClone(context);
		double score = Float.NEGATIVE_INFINITY;

		if (System.currentTimeMillis() - startMillis > timeout) {
			return score;
		}

		if (simulation.isDisposed()) {
			return Float.NEGATIVE_INFINITY;
		}
		simulation.getLogic().performGameAction(playerId, action);
		if (depth == 0 || simulation.getActivePlayerId() != playerId || simulation.gameDecided()) {
			return heuristic.getScore(simulation, playerId);
		}

		List<GameAction> validActions = simulation.getValidActions();


		for (GameAction gameAction : validActions) {
			score = Math.max(score, alphaBeta(simulation, playerId, gameAction, depth - 1, startMillis));
			if (score >= 100000) {
				break;
			}
		}

		return score;
	}

	private GameContext getClone(GameContext original) {
		GameContext context = original.clone();
		// Assume that the players are GameStateValueBehaviour players
		context.getPlayer1().setBehaviour(new GameStateValueBehaviour(featureVector, nameSuffix));
		context.getPlayer2().setBehaviour(new GameStateValueBehaviour(featureVector, nameSuffix));
		return context;
	}

	private void answerTrainingData(TrainingData trainingData) {
		featureVector = trainingData != null ? trainingData.getFeatureVector() : FeatureVector.getFittest();
		heuristic = new ThreatBasedHeuristic(featureVector);
		nameSuffix = trainingData != null ? "(trained)" : "(untrained)";
	}

	@Override
	public Behaviour clone() {
		if (featureVector != null) {
			return new GameStateValueBehaviour(featureVector.clone(), nameSuffix);
		}
		return new GameStateValueBehaviour();
	}

	@Override
	public String getName() {
		return "Game state value " + nameSuffix;
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		requestTrainingData(player);
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() > 3) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		long startMillis = System.currentTimeMillis();
		if (validActions.size() == 1) {
			return validActions.get(0);
		}

		int depth = 2;
		// when evaluating battlecry and discover actions, only optimize the immediate value
		if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
			depth = 0;
		} else if (validActions.get(0).getActionType() == ActionType.DISCOVER) {
			return validActions.get(0);
		}

		GameAction bestAction = validActions.get(0);
		double bestScore = Double.NEGATIVE_INFINITY;

		for (GameAction gameAction : validActions) {
			double score = alphaBeta(context, player.getId(), gameAction, depth, startMillis);
			if (score > bestScore) {
				bestAction = gameAction;
				bestScore = score;
			}
		}

		logger.debug("Selecting best action {} with score {}", bestAction, bestScore);

		return bestAction;
	}

	private void requestTrainingData(Player player) {
		if (heuristic != null) {
			return;
		}

		RequestTrainingDataNotification request = new RequestTrainingDataNotification(player.getDeckName(), this::answerTrainingData);
		NotificationProxy.notifyObservers(request);
	}

}
