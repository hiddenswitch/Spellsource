package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;

/**
 * Evaluates to {@code true} if the {@code target} is specifically the {@link net.demilich.metastone.game.entities.minions.Race#ALL}.
 */
public final class AmalgamRaceCondition extends Condition {

	public AmalgamRaceCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return target != null && target.getRace().equals(Race.ALL);
	}
}
