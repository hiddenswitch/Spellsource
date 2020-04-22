package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When in play, aftermaths fire twice.
 */
public class DoubleDeathrattlesAura extends AbstractFriendlyCardAura {

	public DoubleDeathrattlesAura(AuraDesc desc) {
		super(desc);
	}
}
