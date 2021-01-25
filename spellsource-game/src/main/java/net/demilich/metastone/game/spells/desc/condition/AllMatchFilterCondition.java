package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if all the {@link ConditionArg#TARGET} entities pass the {@link ConditionArg#FILTER}.
 *
 * @see AnyMatchFilterCondition for a version of this condition where <b>any</b> matching filter causes the condition to
 * evaluate to {@code true}.
 */
public class AllMatchFilterCondition extends Condition {

	public AllMatchFilterCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return true;
	}

	@Override
	protected boolean singleTargetOnly() {
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}

	@Override
	protected boolean usesFilter() {
		return true;
	}

	@Override
	protected boolean requiresAtLeastOneTarget() {
		return true;
	}
}
