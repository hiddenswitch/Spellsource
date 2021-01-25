package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class BeforeMinionPlayedTrigger extends BeforeMinionSummonedTrigger {
	public BeforeMinionPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean onlyPlayedFromHandOrDeck() {
		return true;
	}
}
