package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.RemoveAttributeSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.ArmorChangedTrigger;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

public class AttributeAura extends Aura {

	public AttributeAura(AuraDesc desc) {
		super(desc.getSecondaryTrigger() != null ? desc.getSecondaryTrigger().create() : null, AddAttributeSpell.create(desc.getAttribute()), RemoveAttributeSpell.create(desc.getAttribute()), desc.getTarget(), (EntityFilter) desc.get(AuraArg.FILTER), desc.getCondition());
	}
}

