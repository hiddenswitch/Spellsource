package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if the player has overloaded and not yet locked Lun this turn.
 *
 * @see OverloadedManaCrystalsCondition for lun locked.
 */
public class OverloadedCondition extends Condition {

	public OverloadedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return player.getAttributeValue(Attribute.OVERLOAD) > 0;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
