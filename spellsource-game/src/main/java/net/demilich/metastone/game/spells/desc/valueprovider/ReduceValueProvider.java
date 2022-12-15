package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Performs {@link ValueProviderArg#OPERATION} on the values returned by {@link ValueProviderArg#VALUE1} applied to each
 * entity resolved by {@link ValueProviderArg#TARGET}.
 * <p>
 * If a filter is specified in {@link ValueProviderArg#FILTER}, apply it to the list of entities resolved by the {@link
 * ValueProviderArg#TARGET}.
 * <p>
 * Specifying an attribute {@link ValueProviderArg#ATTRIBUTE} is a shorthand for a {@code "value1"} set to an {@link
 * AttributeValueProvider} with the specified attribute.
 * <p>
 * If a {@link ValueProviderArg#VALUE2} is specified and the number of resolve target entities is zero, returns this
 * value.
 */
public class ReduceValueProvider extends ValueProvider {

	private static Logger logger = LoggerFactory.getLogger(ReduceValueProvider.class);

	public ReduceValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	public static ValueProviderDesc create(EntityReference target, Attribute attribute, EntityFilter filter, AlgebraicOperation operation) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(ReduceValueProvider.class);
		arguments.put(ValueProviderArg.TARGET, target);
		arguments.put(ValueProviderArg.FILTER, filter);
		arguments.put(ValueProviderArg.ATTRIBUTE, attribute);
		arguments.put(ValueProviderArg.OPERATION, operation);

		return new ValueProviderDesc(arguments);
	}

	public static ValueProviderDesc create(EntityReference target, ValueProviderDesc value1, EntityFilter filter, AlgebraicOperation operation) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(ReduceValueProvider.class);
		arguments.put(ValueProviderArg.TARGET, target);
		arguments.put(ValueProviderArg.FILTER, filter);
		arguments.put(ValueProviderArg.VALUE1, value1.create());
		arguments.put(ValueProviderArg.OPERATION, operation);
		return new ValueProviderDesc(arguments);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		EntityReference sourceReference = (EntityReference) getDesc().get(ValueProviderArg.TARGET);
		Attribute attribute = (Attribute) getDesc().get(ValueProviderArg.ATTRIBUTE);
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

		AlgebraicOperation operation = getDesc().containsKey(ValueProviderArg.OPERATION) ?
				(AlgebraicOperation) getDesc().get(ValueProviderArg.OPERATION)
				: AlgebraicOperation.MAXIMUM;


		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		if (filter != null) {
			entities.removeIf(filter.matcher(context, player, host).negate());
		}

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
			throw new UnsupportedOperationException("Cannot do a reduce with a DIVIDE, MODULO or NEGATE operator.");
		} else {
			value = 0;
		}

		if (entities.isEmpty() && getDesc().containsKey(ValueProviderArg.VALUE2)) {
			return getDesc().getValue(ValueProviderArg.VALUE2, context, player, target, host, 0);
		}

		for (Entity entity : entities) {
			boolean isApplyingValueProvider = getDesc().containsKey(ValueProviderArg.VALUE1) &&
					ValueProvider.class.isAssignableFrom(getDesc().get(ValueProviderArg.VALUE1).getClass());
			if (isApplyingValueProvider) {
				ValueProvider targetValueProvider = (ValueProvider) getDesc().get(ValueProviderArg.VALUE1);
				value = operation.performOperation(value, targetValueProvider.getValue(context, player, entity, target));
			} else if (attribute != null) {
				int value1 = AttributeValueProvider.create(attribute, entity.getReference()).create().getValue(context, player, entity, host);
				value = operation.performOperation(value, value1);
			} else {
				value = operation.performOperation(value, getDesc().getInt(ValueProviderArg.VALUE1));
			}

		}

		return value;
	}

}
