package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * This condition is fulfilled if the {@link GameLogic#randomBool()} method returns {@code true}.
 * <p>
 * Used to simulate flipping a coin or "50% chance" effects.
 */
public final class RandomCondition extends Condition {

	public RandomCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	public boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		return context.getLogic().randomBool();
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
