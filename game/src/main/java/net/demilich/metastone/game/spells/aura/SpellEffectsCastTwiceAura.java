package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.CardPlayedTrigger;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

public class SpellEffectsCastTwiceAura extends Aura {

	public SpellEffectsCastTwiceAura(AuraDesc desc) {
		super(desc);
		this.triggers.add(CardPlayedTrigger.create().create());
		this.triggers.add(new WillEndSequenceTrigger());
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}
}
