package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * As long as the {@link net.demilich.metastone.game.Player} entity that matches the {@code playerId} of an effect is in
 * this aura's {@link #affectedEntities}, spells whose key/value pairs are a superset of the {@link #removeAuraEffect}
 * have their key/values overwritten by the spells in {@link #applyAuraEffect}.
 */
public class SpellOverrideAura extends Aura {
	public SpellOverrideAura(AuraDesc desc) {
		super(new WillEndSequenceTrigger(), NullSpell.create(), NullSpell.create(), desc.getTarget(), desc.getFilter(), desc.getCondition());
		if (desc.containsKey(AuraArg.SECONDARY_TRIGGER)) {
			this.getTriggers().add(((EventTriggerDesc) desc.get(AuraArg.SECONDARY_TRIGGER)).create());
		}
		setDesc(desc);
	}
}

