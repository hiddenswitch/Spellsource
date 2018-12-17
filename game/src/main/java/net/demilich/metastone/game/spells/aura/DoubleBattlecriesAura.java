package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class DoubleBattlecriesAura extends Aura {

	private static final long serialVersionUID = -5320539865472276291L;
	public SpellDesc extraEffect = null;

    public DoubleBattlecriesAura(AuraDesc desc) {
        super(desc);
        this.triggers.add(new WillEndSequenceTrigger());
        if (desc.containsKey(AuraArg.REMOVE_EFFECT)) {
            extraEffect = (SpellDesc) desc.get(AuraArg.REMOVE_EFFECT);
        }
        applyAuraEffect = NullSpell.create();
        removeAuraEffect = NullSpell.create();
    }
}
