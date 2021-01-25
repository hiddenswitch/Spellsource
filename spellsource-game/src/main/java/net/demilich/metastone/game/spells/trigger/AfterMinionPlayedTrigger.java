package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterMinionPlayedTrigger extends AfterMinionSummonedTrigger {

	public AfterMinionPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean onlyPlayedFromHandOrDeck() {
		return true;
	}

}
