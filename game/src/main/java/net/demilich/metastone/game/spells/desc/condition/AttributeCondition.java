package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.valueprovider.AttributeValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

public class AttributeCondition extends Condition {

	public AttributeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		EntityReference entityReference = (EntityReference) desc.get(ConditionArg.TARGET);
		Entity entity = null;
		if (entityReference == null) {
			entity = target;
		} else {
			List<Entity> entities = context.resolveTarget(player, source, entityReference);
			if (entities == null || entities.isEmpty()) {
				return false;
			}
			entity = entities.get(0);
		}

		Attribute attribute = (Attribute) desc.get(ConditionArg.ATTRIBUTE);
		ComparisonOperation operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);
		if (operation == null || operation == ComparisonOperation.HAS) {
			return entity.hasAttribute(attribute);
		}

		int targetValue = desc.getValue(ConditionArg.VALUE, context, player, entity, source, 0);
		int actualValue = AttributeValueProvider.provideValueForAttribute(context, attribute, entity);

		return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
	}

}
