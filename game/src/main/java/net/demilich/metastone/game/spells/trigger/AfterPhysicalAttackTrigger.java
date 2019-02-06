package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterPhysicalAttackTrigger extends PhysicalAttackTrigger {

	public AfterPhysicalAttackTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_PHYSICAL_ATTACK;
	}
}
