package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * Counts the remaining number of fires in a maxFires context unless there are none, in which case provides {@link
 * ValueProviderArg#VALUE}.
 * <p>
 * The {@code host} specifies the {@link Enchantment} containing the {@link Enchantment#getMaxFires()}.
 */
public class FiresLeftValueProvider extends ValueProvider {

	public FiresLeftValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(GameContext context, Player player, Entity target, Entity host, int value) {
		var resolvedHost = context.resolveSingleTarget(host.getReference(), false);
		if (resolvedHost instanceof Enchantment enchantment && enchantment.getMaxFires() != null) {
			return Math.max(0, enchantment.getMaxFires() - enchantment.getFires());
		}
		return value;
	}
}
