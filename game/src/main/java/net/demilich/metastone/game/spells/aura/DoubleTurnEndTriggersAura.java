package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this aura is in play, abilities triggered by the turn ending trigger twice.
 */
public final class DoubleTurnEndTriggersAura extends Aura {

	public DoubleTurnEndTriggersAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}
}
