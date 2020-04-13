package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class DoubleDeathrattlesAura extends Aura {

	public DoubleDeathrattlesAura(AuraDesc desc) {
        super(desc);
        this.getTriggers().add(new WillEndSequenceTrigger());
        applyAuraEffect = NullSpell.create();
        removeAuraEffect = NullSpell.create();
    }
}
