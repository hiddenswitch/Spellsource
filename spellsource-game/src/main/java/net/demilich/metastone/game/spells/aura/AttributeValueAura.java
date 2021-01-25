package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.ModifyAttributeSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

/**
 * Modifies the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#ATTRIBUTE} an amount specified in {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#VALUE}, which must be an integer.
 */
public class AttributeValueAura extends SpellAura {

	public AttributeValueAura(AuraDesc desc) {
		super(desc);
		setApplyAuraEffect(ModifyAttributeSpell.create(desc.getAttribute(), desc.getValue()));
		setRemoveAuraEffect(ModifyAttributeSpell.create(desc.getAttribute(), -desc.getValue()));
	}
}
