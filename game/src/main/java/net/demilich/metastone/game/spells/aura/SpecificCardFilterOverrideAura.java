package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * This aura tricks {@link SpecificCardFilter} into thinking the affected entities are actually {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#CARD}.
 */
public class SpecificCardFilterOverrideAura extends Aura {
	private static final long serialVersionUID = 1331015558887268418L;

	public SpecificCardFilterOverrideAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
	}
}
