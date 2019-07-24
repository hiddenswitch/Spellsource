package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.PhysicalAttackEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class PhysicalAttackTrigger extends EventTrigger {

	public PhysicalAttackTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		PhysicalAttackEvent physicalAttackEvent = (PhysicalAttackEvent) event;

		Race race = (Race) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !physicalAttackEvent.getDefender().getRace().hasRace(race)) {
			return false;
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.PHYSICAL_ATTACK;
	}
}
