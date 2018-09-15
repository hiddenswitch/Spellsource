package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class TargetSelectionOverrideAura extends Aura {
	public TargetSelectionOverrideAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), desc.getApplyEffect(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
        /*
        super(desc);
        desc.replace(AuraArg.REMOVE_EFFECT, NullSpell.create());
        setDesc(desc);
        */
	}
}
