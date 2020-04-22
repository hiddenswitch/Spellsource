package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * {@code true} if the {@code target} {@link Entity#isDestroyed()}.
 */
public class IsDeadCondition extends Condition {

	public IsDeadCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return target.isDestroyed();
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
