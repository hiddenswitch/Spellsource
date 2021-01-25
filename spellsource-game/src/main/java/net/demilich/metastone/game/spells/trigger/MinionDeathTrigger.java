package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.events.KillEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.TargetType;

/**
 * A trigger that fires whenever a minion dies.
 * <p>
 * A dead minion hosting such a trigger will never trigger for itself, because the {@link KillEvent} this trigger
 * listens to fires when the dead minion is already in the graveyard.
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
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		KillEvent killEvent = (KillEvent) event;
		if (killEvent.getTarget().getEntityType() != EntityType.MINION) {
			return false;
		}

		if (killEvent.getTarget().hasAttribute(Attribute.PERMANENT)) {
			return false;
		}

		Minion minion = (Minion) killEvent.getTarget();

		String race = (String) getDesc().get(EventTriggerArg.RACE);
		if (race != null && !Race.hasRace(event.getGameContext(), minion, race)) {
			return false;
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.KILL;
	}

}
