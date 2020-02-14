package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Gives a bonus effect to the player's supremacies specified in this aura's {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#APPLY_EFFECT}.
 *
 * @see net.demilich.metastone.game.spells.SupremacySpell for more about supremacies.
 */
public final class SupremacyBonusEffectAura extends AbstractFriendlyCardAura {

	public SupremacyBonusEffectAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
	}
}
