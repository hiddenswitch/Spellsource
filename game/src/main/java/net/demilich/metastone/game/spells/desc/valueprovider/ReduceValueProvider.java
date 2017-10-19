package net.demilich.metastone.game.spells.desc.valueprovider;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

public class ReduceValueProvider extends ValueProvider {
	public ReduceValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference sourceReference = (EntityReference) desc.get(ValueProviderArg.TARGET);
		Attribute attribute = (Attribute) desc.get(ValueProviderArg.ATTRIBUTE);
		List<Entity> entities = null;
		if (sourceReference != null) {
			entities = context.resolveTarget(player, host, sourceReference);
		} else {
			entities = new ArrayList<>();
			entities.add(target);
		}
		if (entities == null) {
			return 0;
		}

		AlgebraicOperation operation = desc.containsKey(ValueProviderArg.OPERATION) ?
				(AlgebraicOperation) desc.get(ValueProviderArg.OPERATION)
				: AlgebraicOperation.MAXIMUM;


		EntityFilter filter = (EntityFilter) desc.get(ValueProviderArg.FILTER);
		int value;

		if (operation == AlgebraicOperation.MAXIMUM) {
			value = Integer.MIN_VALUE;
		} else if (operation == AlgebraicOperation.MINIMUM) {
			value = Integer.MAX_VALUE;
		} else if (operation == AlgebraicOperation.MULTIPLY) {
			value = 1;
		} else if (operation == AlgebraicOperation.DIVIDE
				|| operation == AlgebraicOperation.MODULO
				|| operation == AlgebraicOperation.NEGATE) {
			throw new UnsupportedOperationException("Cannot (or ill-advised) to do a reduce with a DIVIDE, MODULO or NEGATE operator.");
		} else {
			value = 0;
		}

		for (Entity entity : entities) {
			if (filter != null && !filter.matches(context, player, entity)) {
				continue;
			}

			boolean isApplyingValueProvider = desc.containsKey(ValueProviderArg.VALUE_1) &&
					ValueProvider.class.isAssignableFrom(desc.get(ValueProviderArg.VALUE_1).getClass());
			if (isApplyingValueProvider) {
				ValueProvider targetValueProvider = (ValueProvider) desc.get(ValueProviderArg.VALUE_1);
				value = operation.performOperation(value, targetValueProvider.getValue(context, player, entity, target));
			} else if (attribute != null) {
				int value1 = AttributeValueProvider.create(attribute, entity.getReference()).createInstance().getValue(context, player, entity, host);
				value = operation.performOperation(value, value1);
			} else {
				value = operation.performOperation(value, desc.getInt(ValueProviderArg.VALUE_1));
			}

		}

		return value;
	}

}
