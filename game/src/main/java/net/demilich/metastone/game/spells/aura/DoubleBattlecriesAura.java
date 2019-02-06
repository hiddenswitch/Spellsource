package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When this aura is in play, battlecries trigger twice.
 */
public final class DoubleBattlecriesAura extends Aura {

	public DoubleBattlecriesAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}
}
