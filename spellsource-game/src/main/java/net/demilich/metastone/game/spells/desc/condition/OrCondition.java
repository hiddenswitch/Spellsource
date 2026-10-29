package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Evaluates to {@code true} if at least one of the conditions in {@link ConditionArg#CONDITIONS} evaluates to {@code
 * true}.
 */
public class OrCondition extends Condition {

	public static Condition create(Condition... conditions) {
		ConditionDesc desc = new ConditionDesc(OrCondition.class);
		desc.put(ConditionArg.CONDITIONS, conditions);
		return desc.create();
	}

	public OrCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition[] conditions) {
		if (conditions == null) {
			return false;
		}
		for (Condition condition : conditions) {
			if (condition.isFulfilled(context, player, source, target)) {
				return true;
			}
		}

		return false;
	}

}
