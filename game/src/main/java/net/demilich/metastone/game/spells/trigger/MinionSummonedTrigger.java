package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class MinionSummonedTrigger extends AbstractSummonTrigger {

	private static final long serialVersionUID = -6791102951405551638L;

	public MinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SUMMON;
	}

}


