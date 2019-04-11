package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Actors affected by this aura will get spells cast on them twice if the {@link AuraArg#SPELL_CONDITION} is met on the
 * casting spell.
 */
public final class TheliaSilentdreamerAura extends Aura {

	public TheliaSilentdreamerAura(AuraDesc desc) {
		super(desc);
		setDesc(desc);
		applyAuraEffect = NullSpell.create();
		removeAuraEffect = NullSpell.create();
	}

	public Condition getSpellCondition() {
		return (Condition) getDesc().get(AuraArg.SPELL_CONDITION);
	}
}
