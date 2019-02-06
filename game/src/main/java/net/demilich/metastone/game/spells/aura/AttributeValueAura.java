package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Modifies the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#ATTRIBUTE} an amount specified in {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#VALUE}, which must be an integer.
 */
public final class AttributeValueAura extends Aura {

	public AttributeValueAura(AuraDesc desc) {
		super(desc);
		this.applyAuraEffect = ModifyAttributeSpell.create(desc.getAttribute(), desc.getValue());
		this.removeAuraEffect = ModifyAttributeSpell.create(desc.getAttribute(), -desc.getValue());
	}
}
