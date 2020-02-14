package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When this aura is in play, spells with minion targets also target adjacent minions.
 */
public class SpellTargetsAdjacentAura extends AbstractFriendlyCardAura {

	public SpellTargetsAdjacentAura(AuraDesc desc) {
		super(desc);
	}
}
