package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this aura is in play, abilities triggered by the turn ending trigger twice.
 */
public final class DoubleTurnEndTriggersAura extends EffectlessAura {

	public DoubleTurnEndTriggersAura(AuraDesc desc) {
		super(desc);
	}
}
