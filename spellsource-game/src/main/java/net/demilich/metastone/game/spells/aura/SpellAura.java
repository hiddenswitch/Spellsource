package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

public abstract class SpellAura extends Aura {

	protected SpellDesc applyAuraEffect;
	protected SpellDesc removeAuraEffect;

	public SpellAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	public SpellDesc getApplyAuraEffect() {
		return applyAuraEffect;
	}

	@Override
	public SpellDesc getRemoveAuraEffect() {
		return removeAuraEffect;
	}

	protected SpellAura setApplyAuraEffect(SpellDesc applyAuraEffect) {
		this.applyAuraEffect = applyAuraEffect;
		return this;
	}

	protected SpellAura setRemoveAuraEffect(SpellDesc removeAuraEffect) {
		this.removeAuraEffect = removeAuraEffect;
		return this;
	}
}
