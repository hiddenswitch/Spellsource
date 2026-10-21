package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * Flyweight {@link ValueProvider} wrapping a constant integer value so that Option 1 typed component methods
 * declaring a lazy {@link ValueProvider} parameter can seamlessly receive either a dynamic {@link ValueProvider}
 * or a constant {@code int} stored in a {@code ParseValueType.VALUE} argument.
 */
public final class ConstValueProvider extends ValueProvider {
	private static final int CACHE_MIN = -16;
	private static final int CACHE_MAX = 64;
	private static final ConstValueProvider[] CACHE = new ConstValueProvider[CACHE_MAX - CACHE_MIN + 1];

	static {
		for (int i = CACHE_MIN; i <= CACHE_MAX; i++) {
			CACHE[i - CACHE_MIN] = new ConstValueProvider(i);
		}
	}

	private final int constantValue;

	public ConstValueProvider(ValueProviderDesc desc) {
		super(desc);
		this.constantValue = desc != null ? desc.getInt(ValueProviderArg.VALUE) : 0;
	}

	private ConstValueProvider(int constantValue) {
		super(createDesc(constantValue));
		this.constantValue = constantValue;
	}

	private static ValueProviderDesc createDesc(int constantValue) {
		ValueProviderDesc desc = new ValueProviderDesc(ConstValueProvider.class);
		desc.put(ValueProviderArg.VALUE, constantValue);
		desc.freeze();
		return desc;
	}

	public static ConstValueProvider of(int value) {
		if (value >= CACHE_MIN && value <= CACHE_MAX) {
			return CACHE[value - CACHE_MIN];
		}
		return new ConstValueProvider(value);
	}

	public static ValueProvider fromObject(Object raw) {
		if (raw == null) {
			return null;
		}
		if (raw instanceof ValueProvider vp) {
			return vp;
		}
		if (raw instanceof Number n) {
			return of(n.intValue());
		}
		return (ValueProvider) raw;
	}

	public int getConstantValue() {
		return constantValue;
	}

	@Override
	public int getValue(GameContext context, Player player, Entity target, Entity host) {
		return constantValue;
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return constantValue;
	}
}
