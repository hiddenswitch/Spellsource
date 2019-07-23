package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BeforeTurnEndTrigger extends EventTrigger {

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		EventTriggerDesc desc = new EventTriggerDesc(TurnEndTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return desc;
	}

	public BeforeTurnEndTrigger() {
		this(new EventTriggerDesc(TurnEndTrigger.class));
	}

	public BeforeTurnEndTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public BeforeTurnEndTrigger(TargetPlayer targetPlayer) {
		this();
		getDesc().put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.BEFORE_TURN_END;
	}
}
