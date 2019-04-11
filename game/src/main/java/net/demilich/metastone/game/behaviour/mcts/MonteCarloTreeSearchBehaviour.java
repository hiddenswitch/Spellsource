package net.demilich.metastone.game.behaviour.mcts;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.IntelligentBehaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An experimental implementation of a Monte Carlo Tree Search behaviour. It is untested and probably incomplete.
 */
public class MonteCarloTreeSearchBehaviour extends IntelligentBehaviour {

	private final static Logger logger = LoggerFactory.getLogger(MonteCarloTreeSearchBehaviour.class);

	private static final int ITERATIONS = 500;
	@Override
	public String getName() {
		return "MCTS";
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

		Node root = new Node(null, player.getId());
		root.initState(context, validActions);
		UctPolicy treePolicy = new UctPolicy();
		for (int i = 0; i < ITERATIONS; i++) {
			root.process(treePolicy);
		}

		return root.getBestAction();
	}

}
