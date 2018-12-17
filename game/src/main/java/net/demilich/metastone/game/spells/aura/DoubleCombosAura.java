package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class DoubleCombosAura extends Aura {

	private static final long serialVersionUID = 7936616385081221393L;

	public DoubleCombosAura(AuraDesc desc) {
        super(desc);
        this.triggers.add(new WillEndSequenceTrigger());
        applyAuraEffect = NullSpell.create();
        removeAuraEffect = NullSpell.create();
    }
}
