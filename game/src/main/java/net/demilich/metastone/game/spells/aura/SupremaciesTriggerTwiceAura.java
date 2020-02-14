package net.demilich.metastone.game.spells.aura;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.WillEndSequenceTrigger;

/**
 * When this aura is active, subspells of {@link net.demilich.metastone.game.spells.SupremacySpell} will be cast twice.
 */
public final class SupremaciesTriggerTwiceAura extends AbstractFriendlyCardAura {

	public SupremaciesTriggerTwiceAura(AuraDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	public void onGameEvent(GameEvent event) {
	}
}

