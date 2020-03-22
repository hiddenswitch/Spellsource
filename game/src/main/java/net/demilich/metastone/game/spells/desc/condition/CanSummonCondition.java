package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Evaluates to {@code true} if the {@code player} can summon more minions.
 */
public class CanSummonCondition extends Condition {

	public CanSummonCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return context.getLogic().canSummonMoreMinions(player);
	}
}
