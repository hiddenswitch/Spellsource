package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.GatekeeperShaAura;

/**
 * Returns the count of Gatekeeper Sha auras (unexpired) that are in play on your side of the battlefield.
 */
public class GatekeeperShaValueProvider extends ValueProvider {

	public GatekeeperShaValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity host) {
		return SpellUtils.getAuras(context, player.getId(), GatekeeperShaAura.class)
				.stream()
				.mapToInt(GatekeeperShaAura::getNumbersIncrease)
				.sum();
	}
}
