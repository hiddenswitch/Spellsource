package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Map;

public class PreGameStartTrigger extends EventTrigger {
	private static final long serialVersionUID = 6215994937201026809L;

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		Map<EventTriggerArg, Object> arguments = new EventTriggerDesc(PreGameStartTrigger.class);
		arguments.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return new EventTriggerDesc(arguments);
	}

	public PreGameStartTrigger() {
		this(new EventTriggerDesc(PreGameStartTrigger.class));
	}

	public PreGameStartTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.PRE_GAME_START;
	}

}
