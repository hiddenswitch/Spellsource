package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class DamageReceivedTrigger extends EventTrigger {

	private static final long serialVersionUID = 5574705941064562631L;

	public DamageReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new EventTriggerDesc(DamageReceivedTrigger.class).create();
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null && damageEvent.getVictim().getEntityType() != targetEntityType) {
			return false;
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.DAMAGE;
	}

}
