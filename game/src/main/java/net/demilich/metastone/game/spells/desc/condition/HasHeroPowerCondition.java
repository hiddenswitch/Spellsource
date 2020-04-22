package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} when the {@link ConditionArg#TARGET_PLAYER} skill is {@link ConditionArg#CARD}.
 */
public class HasHeroPowerCondition extends Condition {

	public HasHeroPowerCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var heroPower = player.getHeroPowerZone().get(0);
		if (heroPower.getCardId() == null) {
			return false;
		}
		var cardId = (String) desc.get(ConditionArg.CARD);
		return heroPower.getCardId().equalsIgnoreCase(cardId);
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
