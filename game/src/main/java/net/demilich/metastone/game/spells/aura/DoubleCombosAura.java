package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * When this is in play, {@link net.demilich.metastone.game.cards.Attribute#COMBO} cards with {@link
 * net.demilich.metastone.game.spells.ComboSpell} decorated spells cast twice.
 */
public class DoubleCombosAura extends EffectlessAura {

	public DoubleCombosAura(AuraDesc desc) {
		super(desc);
	}
}
