package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class AfterTurnStartTrigger extends EventTrigger {

	public AfterTurnStartTrigger() {
		this(new EventTriggerDesc(AfterTurnStartTrigger.class));
	}

	public AfterTurnStartTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		EventTriggerDesc desc = new EventTriggerDesc(AfterTurnStartTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return desc;
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_TURN_START;
	}

}
