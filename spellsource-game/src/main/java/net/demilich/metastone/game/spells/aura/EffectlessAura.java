package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

public abstract class EffectlessAura extends Aura {
	private static final SpellDesc NULL = NullSpell.create();

	public EffectlessAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	public SpellDesc getApplyAuraEffect() {
		return NULL;
	}

	@Override
	public SpellDesc getRemoveAuraEffect() {
		return NULL;
	}
}
