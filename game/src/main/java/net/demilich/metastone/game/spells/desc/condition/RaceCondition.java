package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Evaluates to {@code true} if the {@code target} or single entity resolved by {@link ConditionArg#TARGET} has the
 * specified {@link ConditionArg#RACE}.
 */
public final class RaceCondition extends Condition {

	public RaceCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		String race = (String) desc.get(ConditionArg.RACE);
		if (desc.containsKey(ConditionArg.TARGET)) {
			target = context.resolveSingleTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET));
		}
		return Race.hasRace(context, target, race);
	}
}
