package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class SupremacyTrigger extends AfterPhysicalAttackTrigger {

	public SupremacyTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return event.getTarget() != null && event.getTarget().getEntityType() == EntityType.MINION;
	}

	@Override
	protected boolean hostConditionMet(GameEvent event, Entity host) {
		if (host.getEntityType() == EntityType.WEAPON) {
			return host.getOwner() == event.getSource().getOwner() && event.getSource().getEntityType() == EntityType.HERO;
		} else {
			return event.getSource() == host;
		}
	}

	@Override
	public boolean fires(GameEvent event, Entity host, int playerId) {
		return event.getTarget() != null && event.getTarget().isDestroyed();
	}
}

