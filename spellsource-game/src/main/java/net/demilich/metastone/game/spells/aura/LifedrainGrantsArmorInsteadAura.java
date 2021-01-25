package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When in play, source cards affected by this aura will grant armor instead of lifedrain.
 */
public final class LifedrainGrantsArmorInsteadAura extends AbstractFriendlyCardAura {

	public LifedrainGrantsArmorInsteadAura(AuraDesc desc) {
		super(desc);
	}
}

