package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Entities affected by this aura are targetable by spells and skills but the effects of those spells and skills are
 * neutralized.
 */
public final class IncorruptibilityAura extends EffectlessAura {

	public IncorruptibilityAura(AuraDesc desc) {
		super(desc);
	}
}
