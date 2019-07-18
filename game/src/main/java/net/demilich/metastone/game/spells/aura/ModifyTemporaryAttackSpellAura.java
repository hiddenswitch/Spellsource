package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * Whenever a card affected by this aura has a {@link net.demilich.metastone.game.spells.TemporaryAttackSpell} effect,
 * the value of temporary attack is increased by the {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#VALUE}.
 */
public class ModifyTemporaryAttackSpellAura extends Aura {

	public ModifyTemporaryAttackSpellAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		setDesc(desc);
	}
}
