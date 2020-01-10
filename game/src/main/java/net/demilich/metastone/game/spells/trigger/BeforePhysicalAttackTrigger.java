package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforePhysicalAttackTrigger extends PhysicalAttackTrigger {

	public BeforePhysicalAttackTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.BEFORE_PHYSICAL_ATTACK;
	}
}
