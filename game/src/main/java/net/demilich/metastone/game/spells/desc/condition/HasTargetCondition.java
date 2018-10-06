package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

public class HasTargetCondition extends Condition {

	public HasTargetCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		/* This is overthinking the intention of HasTargetCondition
		Entity playerTarget = context.resolveSingleTarget(player, source, EntityReference.TARGET);
		EntityFilter filter = (EntityFilter) desc.getOrDefault(ConditionArg.FILTER, AndFilter.create());
		return playerTarget != null && filter.matches(context, player, playerTarget, source);
		*/
		return target != null && target.getZone() != Zones.REMOVED_FROM_PLAY;
	}
}
