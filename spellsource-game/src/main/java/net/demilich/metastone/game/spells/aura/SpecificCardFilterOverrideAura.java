package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.filter.SpecificCardFilter;

/**
 * This aura tricks {@link SpecificCardFilter} into thinking the affected entities are actually {@link
 * net.demilich.metastone.game.spells.desc.aura.AuraArg#CARD}.
 */
public class SpecificCardFilterOverrideAura extends EffectlessAura {

	public SpecificCardFilterOverrideAura(AuraDesc desc) {
		super(desc);
	}
}
