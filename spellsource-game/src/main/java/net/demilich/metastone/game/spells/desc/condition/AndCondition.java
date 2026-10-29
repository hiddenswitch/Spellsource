package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class AndCondition extends Condition {

	public AndCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition[] conditions) {
		if (conditions != null) {
			for (Condition condition : conditions) {
				if (!condition.isFulfilled(context, player, source, target)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}
}
