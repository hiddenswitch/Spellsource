package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
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

