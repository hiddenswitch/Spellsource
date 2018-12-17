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
public class RandomCondition extends Condition {

	private static final long serialVersionUID = 707057838829867886L;

	public RandomCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return context.getLogic().randomBool();
	}

}
