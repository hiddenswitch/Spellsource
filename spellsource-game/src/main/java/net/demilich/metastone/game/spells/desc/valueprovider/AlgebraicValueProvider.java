package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Default;

public class AlgebraicValueProvider extends ValueProvider {

	private static int evaluateOperation(int value1, int value2, AlgebraicOperation operation) {
		return operation.performOperation(value1, value2);
	}

	public static AlgebraicValueProvider create(Object value1, Object value2, AlgebraicOperation operation) {
		ValueProviderDesc desc = new ValueProviderDesc(AlgebraicValueProvider.class);
		if (value1 != null) {
			desc.put(ValueProviderArg.VALUE1, value1);
		}
		if (value2 != null) {
			desc.put(ValueProviderArg.VALUE2, value2);
		}
		desc.put(ValueProviderArg.OPERATION, operation);
		return (AlgebraicValueProvider) desc.create();
	}

	public AlgebraicValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity source,
			@Default(1) int value1,
			@Default(1) int value2,
			AlgebraicOperation operation) {
		AlgebraicOperation resolvedOp = operation != null ? operation : AlgebraicOperation.ADD;
		return evaluateOperation(value1, value2, resolvedOp);
	}
}
