package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * Returns the number of fires left in the first enchantment attached to the {@code target}.
 * <p>
 * {@link net.demilich.metastone.game.spells.desc.SpellArg#VALUE} is used as the default value when the card is in the
 * hand.
 */
public class FiresLeftValueProvider extends ValueProvider {

	public FiresLeftValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return context.getTriggersAssociatedWith(target.getReference())
				.stream()
				.filter(Enchantment.class::isInstance)
				.map(Enchantment.class::cast)
				.filter(e -> e.getMaxFires() != null)
				.findFirst()
				.map(e -> e.getMaxFires() - e.getFires())
				.orElse(getDesc().getValue(ValueProviderArg.VALUE, context, player, target, host, 0));
	}
}
