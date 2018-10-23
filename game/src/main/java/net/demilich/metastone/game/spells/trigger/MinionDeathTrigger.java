package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.KillEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.TargetType;

/**
 * A trigger that fires
 */
public final class MinionDeathTrigger extends EventTrigger {

	public MinionDeathTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create(TargetType targetType) {
		EventTriggerDesc inst = new EventTriggerDesc(MinionDeathTrigger.class);
		inst.put(EventTriggerArg.HOST_TARGET_TYPE, targetType);
		return inst;
	}

	/**
	 * Creates a minion death trigger that fires when its host dies.
	 *
	 * @return A new desc
	 */
	public static EventTriggerDesc create() {
		return create(TargetType.IGNORE_OTHER_TARGETS);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		KillEvent killEvent = (KillEvent) event;
		if (killEvent.getVictim().getEntityType() != EntityType.MINION) {
			return false;
		}

		Minion minion = (Minion) killEvent.getVictim();

		Race race = (Race) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !minion.getRace().hasRace(race)) {
			return false;
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.KILL;
	}

}
