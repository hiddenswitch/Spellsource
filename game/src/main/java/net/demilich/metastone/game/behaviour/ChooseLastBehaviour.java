package net.demilich.metastone.game.behaviour;

import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.UtilityBehaviour;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

/**
 * A behaviour which instructs the player to always perform the last available action.
 */
public class ChooseLastBehaviour extends UtilityBehaviour {

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