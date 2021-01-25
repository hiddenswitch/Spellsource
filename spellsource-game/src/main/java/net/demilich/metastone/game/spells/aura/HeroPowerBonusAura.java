package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Gives spells decorated with {@link net.demilich.metastone.game.spells.HeroPowerSpell} the given bonus affect in
 * {@link net.demilich.metastone.game.spells.desc.aura.AuraArg#APPLY_EFFECT}.
 */
public class HeroPowerBonusAura extends EffectlessAura {
	public HeroPowerBonusAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	protected EntityReference getTargets() {
		return EntityReference.FRIENDLY_HERO_POWER;
	}
}

