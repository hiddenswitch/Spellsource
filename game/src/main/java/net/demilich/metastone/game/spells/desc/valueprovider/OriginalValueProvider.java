package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * When used in a {@link net.demilich.metastone.game.spells.aura.SpellOverrideAura}, this value provider returns the
 * original value specified in this key.
 */
public class OriginalValueProvider extends ValueProvider {

	public static ValueProviderDesc create(Object originalValue) {
		if (!(originalValue instanceof Integer || originalValue instanceof ValueProviderArg)) {
			throw new UnsupportedOperationException("Cannot retrieve an original value of the wrong type.");
		}

		ValueProviderDesc desc = new ValueProviderDesc(OriginalValueProvider.class);
		desc.put(ValueProviderArg.VALUE, originalValue);
		return desc;
	}

	public OriginalValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return getDesc().getValue(ValueProviderArg.VALUE, context, player, target, host, 0);
	}
}
