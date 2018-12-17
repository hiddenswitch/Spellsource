package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterMinionSummonedTrigger extends AbstractSummonTrigger {

	private static final long serialVersionUID = -2519328107499131955L;

	public AfterMinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_SUMMON;
	}

}
