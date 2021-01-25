package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.cards.Attribute;

/**
 * {@code true} when the {@code target} or {@link ConditionArg#TARGET} has an attribute {@link ConditionArg#ATTRIBUTE}
 * that evaluates to {@code true} with the {@link ConditionArg#OPERATION}.
 * <p>
 * Only supports single targets.
 * <p>
 * Uses the {@link ComparisonOperation#HAS} by default, or compares with the {@link ConditionArg#VALUE}.
 */
public class AttributeCondition extends Condition {

	public AttributeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var attribute = (Attribute) desc.get(ConditionArg.ATTRIBUTE);
		var operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		if (operation == null || operation == ComparisonOperation.HAS) {
			return target.hasAttribute(attribute);
		}

		var targetValue = desc.getValue(ConditionArg.VALUE, context, player, target, source, 0);
		var actualValue = AttributeValueProvider.provideValueForAttribute(context, attribute, target);

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
