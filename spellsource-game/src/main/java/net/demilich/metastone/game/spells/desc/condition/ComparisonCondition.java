package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

/**
 * Compares the values using the expression {@link ConditionArg#VALUE1} <b> {@link ConditionArg#OPERATION} </b> {@link
 * ConditionArg#VALUE2}.
 */
public final class ComparisonCondition extends Condition {

	public ComparisonCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			int value1,
			int value2,
			ComparisonOperation operation) {
		return SpellUtils.evaluateOperation(operation, value1, value2);
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
