package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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

	protected int provideValue(
			GameContext context,
			Player player,
			Entity targetEntity,
			Entity host,
			EntityReference target,
			Attribute attribute,
			@Default(string = "MAXIMUM") AlgebraicOperation operation,
			EntityFilter filter,
			ValueProvider value1,
			Integer value2) {
		List<Entity> entities = target != null
				? context.resolveTarget(player, host, target)
				: Collections.singletonList(targetEntity);
		if (entities == null) {
			return 0;
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

		boolean matchedAny = false;
		for (int i = 0, n = entities.size(); i < n; i++) {
			Entity entity = entities.get(i);
			if (filter != null && !filter.matches(context, player, entity, host)) {
				continue;
			}
			matchedAny = true;
			if (value1 != null) {
				value = operation.performOperation(value, value1.getValue(context, player, entity, targetEntity));
			} else if (attribute != null) {
				int v1 = AttributeValueProvider.provideValueForAttribute(context, attribute, entity);
				value = operation.performOperation(value, v1);
			} else {
				value = operation.performOperation(value, 0);
			}
		}

		if (!matchedAny && value2 != null) {
			return value2;
		}

		return value;
	}

}
