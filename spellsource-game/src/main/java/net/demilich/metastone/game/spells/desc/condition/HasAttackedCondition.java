package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

/**
 * {@code true} if any of the {@link ConditionArg#TARGET} or {@code target} has attacked this turn according to {@link
 * Attribute#ATTACKS_THIS_TURN}.
 */
public class HasAttackedCondition extends Condition {

	public HasAttackedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return (int) target.getAttributes().getOrDefault(Attribute.ATTACKS_THIS_TURN, 0) > 0;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return true;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return false;
	}
}
