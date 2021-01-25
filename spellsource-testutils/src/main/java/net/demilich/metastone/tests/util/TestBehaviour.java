package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;

public class TestBehaviour extends UtilityBehaviour {

	private EntityReference targetPreference;

	@Override
	public String getName() {
		return "Null Behaviour";
	}

	public EntityReference getTargetPreference() {
		return targetPreference;
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return new ArrayList<Card>();
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (targetPreference != null) {
			for (GameAction action : validActions) {
				if (action.getTargetReference().equals(targetPreference)) {
					return action;
				}
			}
		}

		return validActions.get(0);
	}

	public void setTargetPreference(EntityReference targetPreference) {
		this.targetPreference = targetPreference;
	}

}
