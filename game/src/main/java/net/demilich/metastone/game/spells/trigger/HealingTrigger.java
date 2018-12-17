package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.HealEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class HealingTrigger extends EventTrigger {

	private static final long serialVersionUID = -280708331918647023L;

	public HealingTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		HealEvent healEvent = (HealEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null) {
			if (healEvent.getTarget().getEntityType() != targetEntityType) {
				return false;
			}
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.HEAL;
	}

}
