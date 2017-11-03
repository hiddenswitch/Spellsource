package net.demilich.metastone.game.behaviour;

import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

public class DoNothingBehaviour extends AbstractBehaviour {

	@Override
	public String getName() {
		return "Do Nothing";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return new ArrayList<Card>();
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		return validActions.get(validActions.size() - 1);
	}

}
