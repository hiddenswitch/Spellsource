package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

import java.util.Objects;

/**
 * Implements Thelia Silentdreamer's effect, which is a {@link SpellsCastTwiceAura} as long as the target is itself.
 */
public final class TheliaSilentdreamerAura extends SpellsCastTwiceAura {

	public TheliaSilentdreamerAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	public boolean isFulfilled(GameContext context, Player player, Entity card, Entity target) {
		Entity self = context.resolveSingleTarget(getHostReference());
		return super.isFulfilled(context, player, card, target) && Objects.equals(self, target);
	}
}
