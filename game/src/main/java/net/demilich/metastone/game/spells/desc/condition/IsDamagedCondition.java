package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * This condition is fulfilled if the {@code target} or the single entity resolved by {@link ConditionArg#TARGET} is
 * damaged.
 */
public class IsDamagedCondition extends Condition {

	public IsDamagedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (getDesc().containsKey(ConditionArg.TARGET)) {
			target = context.resolveSingleTarget(player, source, (EntityReference) getDesc().get(ConditionArg.TARGET));
		}
		return ((Actor) target).isWounded();
	}

}

