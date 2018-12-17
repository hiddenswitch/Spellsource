package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforeMinionSummonedTrigger extends AbstractSummonTrigger {

	private static final long serialVersionUID = -4663900737779114064L;

	public BeforeMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.BEFORE_SUMMON;
	}

}
