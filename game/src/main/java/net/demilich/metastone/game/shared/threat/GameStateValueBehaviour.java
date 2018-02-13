package net.demilich.metastone.game.shared.threat;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.heuristic.Heuristic;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.shared.trainingmode.TrainingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * GameStateValueBehaviour is an implementation of a decent AI with the best-in-class performance among bots in the
 * community.
 * <p>
 * The objective of the bot is to determine the best possible action from the list of actions it can choose from, given
 * the current state of the game. In the AI community, this is called a "policy," and it is typically represented in the
 * literature using the Greek letter Pi.
 * <p>
 * Policies take as inputs a list of actions, like "Friendly hero attacks opposing hero," and the current state of the
 * game, in this case all the data available in {@link GameContext}, and returns an action as an output. {@link
 * #requestAction(GameContext, Player, List)} corresponds to this AI's policy; when implementing an idea, especially
 * from the literature, start with {@link #requestAction(GameContext, Player, List)}.
 * <p>
 * In this AI, {@link #requestAction(GameContext, Player, List)} tries each action available to take and scoring the
 * outcome. The score, in this case, depends on the game state. The action that leads to the highest score is the action
 * returned by {@link #requestAction(GameContext, Player, List)}. Since card games typically involve combos (sequences
 * of actions), this AI will actually play out all available actions until the end of its turn, seeking which starting
 * action will maximize its score at the **end** of its turn.
 * <p>
 * Therefore, the best way to describe this AI is: It is a "single turn horizon" AI. That is, it tries to pick actions
 * to maximize a score by the end of the turn.
 * <p>
 * This means the AI doesn't see some effects that start at the beginning of an opponent's turn, like Doomsayer, or of
 * its turn, like the Cursed card. This class special cases cards like these to maintain its basic architecture.
 * Additionally, playing around secrets is difficult without a long-term vision of the game, so enemy secrets are
 * omitted from the simulation entirely.
 * <p>
 * How does the AI do scoring? Clearly, it can't be as simple as, "The highest score is whatever reduces the opponent's
 * health the most." Indeed, this class uses a complex model for a score, called a {@link Heuristic}, which is capable
 * of looking at any factor in the game state to contribute to the score. The specific heuristic used by this class is
 * the {@link ThreatBasedHeuristic}. Visiting that class, it's clear that things like holding onto hard-removal cards,
 * or fully destroying minions, contribute greatly to the score, along with the heroes' and minions' attack and
 * hitpoints.
 * <p>
 * In order to understand the various tradeoffs between actions, the way the {@link Heuristic} is calculated should
 * somehow reflect the actual ability of these actions to lead to victory. Clearly, the score for an action should, in
 * an ideal world, be as simple as, "Whatever maximizes my chance of winning." But for the time being, answering that
 * question is computationally impossible in this card game. Instead, GameStateValueBehaviour makes the assumption that
 * across many games, maximizing some score at the end of my turn maximizes my chance of winning on average. Hearthstone
 * generally rewards great tactical play, so this is a surprisingly robust assumption. But it isn't necessarily true for
 * many games or for all cards in this game. However, this assumption makes this AI fast, so it is preferred in this
 * context.
 * <p>
 * The {@link ThreatBasedHeuristic} tries to maximize the chance of winning by somehow relating its scoring mechanism to
 * the actual outcome of a match. The {@link net.demilich.metastone.game.shared.threat.cuckoo.CuckooLearner} is the
 * system that tweaks the scoring function in order to choose tweaks that corresponded to greater wins in the game. This
 * approach makes GameStateValueBehaviour the best delivered AI in the Hearthstone community.
 *
 * @see #requestAction(GameContext, Player, List) to see how each action of the possible actions is tested for the one
 * with the highest score.
 * @see #alphaBeta(GameContext, int, GameAction, int, long) for the function that tries all actions, until the end of
 * the AI's turn, in order to find the first action in the best sequence of actions.
 */
public class GameStateValueBehaviour extends AbstractBehaviour {
	private final Logger logger = LoggerFactory.getLogger(GameStateValueBehaviour.class);

	protected Heuristic heuristic;
	protected FeatureVector featureVector;
	protected String nameSuffix = "";
	protected long timeout = 3500;

	public GameStateValueBehaviour() {
		this(FeatureVector.getFittest(), "Botty McBotface");
	}

	public GameStateValueBehaviour(FeatureVector featureVector, String nameSuffix) {
		this.featureVector = featureVector;
		this.nameSuffix = nameSuffix;
		this.heuristic = new ThreatBasedHeuristic(featureVector);
	}

	/**
	 * Starting with the given action, try all possible actions up to a certain depth, and return the score of the state
	 * at the end of that sequence.
	 *
	 * @param context     The starting game state to use for this simulation.
	 * @param playerId    The player ID corresponding to this AI.
	 * @param action      The action to test.
	 * @param depth       When greater than zero, visit the next possible action after {@code action}. Otherwise, return
	 *                    the score for the state after executing {@code action}.
	 * @param startMillis The time when this evaluation began. If we've spent too long executing this function, we will
	 *                    return the current score.
	 * @return A score corresponding to this action's best chain of actions.
	 */
	@Suspendable
	private double alphaBeta(GameContext context, int playerId, GameAction action, int depth, long startMillis) {
		GameContext simulation = getClone(context);
		double score = Float.NEGATIVE_INFINITY;
		final boolean timedOut = System.currentTimeMillis() - startMillis > timeout;

		// Don't simulate the opposing player's secrets
		Player opponent = simulation.getOpponent(simulation.getPlayer(playerId));
		simulation.getLogic().removeSecrets(opponent);

		if (simulation.isDisposed()) {
			return Float.NEGATIVE_INFINITY;
		}
		simulation.getLogic().performGameAction(playerId, action);

		// If a Doomsayer is on the board (i.e., not destroyed), don't count friendly minions
		if (simulation.getPlayers().stream().flatMap(p -> p.getMinions().stream()).anyMatch(c -> c.getSourceCard().getCardId().equals("minion_doomsayer"))) {
			simulation.getPlayer(playerId).getMinions().forEach(m -> simulation.getLogic().markAsDestroyed(m));
			simulation.getLogic().endOfSequence();
		}

		if (timedOut || depth == 0 || simulation.getActivePlayerId() != playerId || simulation.updateAndGetGameOver()) {
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

	/**
	 * Returns a clone of the game context, assuming the opponent is a {@link GameStateValueBehaviour} too.
	 *
	 * @param original The original game context to use.
	 * @return The clone.
	 */
	protected GameContext getClone(GameContext original) {
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

	/**
	 * Mulligans for cards, preferring to create an on-curve starting hand.
	 *
	 * @param context The game context.
	 * @param player  The player who's mulliganing.
	 * @param cards   The cards in the player's first hand.
	 * @return A list of cards to discard.
	 */
	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		requestTrainingData(player);
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() > 3) {
				discardedCards.add(card);
			}
		}

		/*
		ListValuedMap<Integer, Card> cardsAtCost = new ArrayListValuedHashMap<>();
		cards.forEach(card -> cardsAtCost.put(card.getBaseManaCost(), card));

		List<Card> discardedCards = new ArrayList<Card>();
		for (int i = 0; i <= cards.size(); i++) {
			if (cardsAtCost.get(i).size() > 1) {
				discardedCards.add(cardsAtCost.get(i).get(0));
			}
		}
		*/

		return discardedCards;
	}

	/**
	 * Requests an action from the GameStateValueBehaviour using a scoring function.
	 * <p>
	 * Suppose the board looked like this:
	 * <p>
	 * <pre>
	 *     Opponent: Warrior, 7 HP, no cards in hand, no minions on the board.
	 *     This Player: Mage, 30 HP, a Fireball in the hand, 6 mana.
	 * </pre>
	 * <p>
	 * Clearly, this player has lethal: Fireballing the opponent, followed by Fireblasting the opponent, will win the
	 * game. There are two sequences of actions in this case that win the game. How does this function wind up returning
	 * the correct actions twice in order to win the game?
	 * <p>
	 * First, at the beginning of the turn, the request action function receives the above game state, followed by the
	 * possible actions:
	 * <p>
	 * <pre>
	 * 1. Fireball opponent.
	 * 2. Fireball yourself.
	 * 3. Fireblast opponent.
	 * 4. Fireblast yourself.
	 * 5. End the turn.
	 * </pre>
	 * <p>
	 * Suppose we scored each action with a simple function: "1 point if the enemy hero is destroyed, otherwise 0."
	 * <p>
	 * <pre>
	 * 1. Fireball opponent = 0 points.
	 * 2. Fireball yourself = 0 points.
	 * 3. Fireblast opponent = 0 points.
	 * 4. Fireblast yourself = 0 points.
	 * 5. End the turn = 0 points.
	 * </pre>
	 * <p>
	 * By just looking at the current actions, it's impossible to see that fireballing or fireblasting your opponent
	 * will lead to victory, even though the scoring function ought to work fine in this particular case.
	 * <p>
	 * What if instead we chose an action based on the score of the state at the end of the SEQUENCE of actions that
	 * particular action can enable? If we expand all the possible actions given our choices, we get:
	 * <p>
	 * <pre>
	 * 1. Fireball opponent = 0 points.
	 *  1. Fireblast opponent = 1 point.
	 *    **1. End turn = 1 point.**
	 *  2. Fireblast yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 2. Fireball yourself = 0 points.
	 *  1. Fireblast opponent = 0 points.
	 *    1. End turn = 0 points.
	 *  2. Fireblast yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 3. Fireblast opponent = 0 points.
	 *  1. Fireball opponent = 1 point.
	 *    **1. End turn = 1 point.**
	 *  2. Fireball yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 4. Fireblast yourself = 0 points.
	 *  1. Fireball opponent = 0 points.
	 *    1. End turn = 0 points.
	 *  2. Fireball yourself = 0 points.
	 *    1. End Turn = 0 points.
	 *  3. End turn = 0 points.
	 * 5. End the turn = 0 points.
	 * </pre>
	 * <p>
	 * When expanding all the possible actions, there are now two sequences of actions that end with 1 point.
	 * <p>
	 * This function will return the FIRST action in the sequence that terminates with the highest score at the end of
	 * the turn. In this example, it will return either action 1 (Fireball opponent) or action 3 (Fireblast opponent).
	 * <p>
	 * The scoring function is much more complicated, but in broad strokes it works the way as described above.
	 *
	 * @param context      The game context where the choice is being made.
	 * @param player       The player who is making the choice.
	 * @param validActions The valid actions the player has to choose from.
	 * @return The action that maximizes the score of the state of the game at the end of this player's turn.
	 * @see ThreatBasedHeuristic for an overview of the scoring function.
	 */
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

	}

}
