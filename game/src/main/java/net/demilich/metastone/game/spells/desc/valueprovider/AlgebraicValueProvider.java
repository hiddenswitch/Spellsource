package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

import java.util.Map;

public class AlgebraicValueProvider extends ValueProvider {

	public static AlgebraicValueProvider create(Object value1, Object value2, AlgebraicOperation operation) {
		Map<ValueProviderArg, Object> arguments = ValueProviderDesc.build(AlgebraicValueProvider.class);
		arguments.put(ValueProviderArg.VALUE_1, value1);
		arguments.put(ValueProviderArg.VALUE_2, value2);
		arguments.put(ValueProviderArg.OPERATION, operation);
		return (AlgebraicValueProvider) (new ValueProviderDesc(arguments).createInstance());
	}

	private static int evaluateOperation(int value1, int value2, AlgebraicOperation operation) {
		return operation.performOperation(value1, value2);
	}

	public AlgebraicValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity source) {
		int value1 = desc.getValue(ValueProviderArg.VALUE_1, context, player, target, null, 1);
		int value2 = desc.getValue(ValueProviderArg.VALUE_2, context, player, target, null, 1);
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(ValueProviderArg.OPERATION);
		return evaluateOperation(value1, value2, operation);
	}

}
