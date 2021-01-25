package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class AlgebraicValueProvider extends ValueProvider {

	public static AlgebraicValueProvider create(Object value1, Object value2, AlgebraicOperation operation) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AlgebraicValueProvider.class);
		if (value1 != null) {
			arguments.put(ValueProviderArg.VALUE1, value1);
		} else {
			arguments.put(ValueProviderArg.VALUE1, 0);
		}
		if (value2 != null) {
			arguments.put(ValueProviderArg.VALUE2, value2);
		} else {
			arguments.put(ValueProviderArg.VALUE2, 0);
		}
		arguments.put(ValueProviderArg.OPERATION, operation);
		return (AlgebraicValueProvider) (new ValueProviderDesc(arguments).create());
	}

	private static int evaluateOperation(int value1, int value2, AlgebraicOperation operation) {
		return operation.performOperation(value1, value2);
	}

	public AlgebraicValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		int value1 = getDesc().getValue(ValueProviderArg.VALUE1, context, player, target, null, 1);
		int value2 = getDesc().getValue(ValueProviderArg.VALUE2, context, player, target, null, 1);
		AlgebraicOperation operation = (AlgebraicOperation) getDesc().get(ValueProviderArg.OPERATION);
		return evaluateOperation(value1, value2, operation);
	}

}
