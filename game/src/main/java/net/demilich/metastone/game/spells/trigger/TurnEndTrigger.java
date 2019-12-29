package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class TurnEndTrigger extends TurnTrigger {

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		EventTriggerDesc desc = new EventTriggerDesc(TurnEndTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return desc;
	}

	public TurnEndTrigger() {
		this(new EventTriggerDesc(TurnEndTrigger.class));
	}

	public TurnEndTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public TurnEndTrigger(TargetPlayer targetPlayer) {
		this();
		getDesc().put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.TURN_END;
	}
}
