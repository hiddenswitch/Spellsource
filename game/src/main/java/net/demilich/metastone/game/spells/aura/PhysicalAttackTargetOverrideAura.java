package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When an {@link net.demilich.metastone.game.entities.Actor} has this aura, its physical attack targets are the
 * entities that are affected by this aura.
 */
public final class PhysicalAttackTargetOverrideAura extends Aura {

	private static final long serialVersionUID = -5192104759409469741L;

	public PhysicalAttackTargetOverrideAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
	}
}
