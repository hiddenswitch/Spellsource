package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.SetAttributeSpell;
import net.demilich.metastone.game.spells.SetCardSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class CardAura extends Aura {

	public CardAura(AuraDesc desc) {
		super(desc.getSecondaryTrigger() != null ? desc.getSecondaryTrigger().create() : null,
				SetCardSpell.create((String) desc.get(AuraArg.CARD_ID), true),
				SetCardSpell.revert(true),
				desc.getTarget(),
				(EntityFilter) desc.get(AuraArg.FILTER),
				desc.getCondition());
	}
}

