package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * As long as the {@link net.demilich.metastone.game.Player} entity that matches the {@code playerId} of an effect is in
 * this aura's {@link #getAffectedEntities()}, spells whose key/value pairs are a superset of the {@link
 * #removeAuraEffect} have their key/values overwritten by the spells in {@link #applyAuraEffect}.
 */
public class SpellOverrideAura extends AbstractFriendlyCardAura {
	public SpellOverrideAura(AuraDesc desc) {
		super(desc);
	}
}

