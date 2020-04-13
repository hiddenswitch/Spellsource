package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class SupremacyTrigger extends AfterPhysicalAttackTrigger {

	public SupremacyTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return event.getEventTarget() != null && event.getEventTarget().getEntityType() == EntityType.MINION;
	}

	@Override
	protected boolean hostConditionMet(GameEvent event, Entity host) {
		if (host.getEntityType() == EntityType.WEAPON) {
			return host.getOwner() == event.getEventSource().getOwner() && event.getEventSource().getEntityType() == EntityType.HERO;
		} else {
			return event.getEventSource() == host;
		}
	}

	@Override
	public boolean fires(GameEvent event) {
		return event.getEventTarget() != null && event.getEventTarget().isDestroyed();
	}
}

