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
public final class SupremacyBonusEffectAura extends Aura {

	private static final long serialVersionUID = 3019915571367880087L;

	public SupremacyBonusEffectAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
	}
}
