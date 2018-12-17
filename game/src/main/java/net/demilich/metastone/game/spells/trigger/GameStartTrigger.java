package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Map;

public class GameStartTrigger extends EventTrigger {
	private static final long serialVersionUID = -3019799353031432393L;

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		Map<EventTriggerArg, Object> arguments = new EventTriggerDesc(GameStartTrigger.class);
		arguments.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return new EventTriggerDesc(arguments);
	}

	public GameStartTrigger() {
		this(new EventTriggerDesc(GameStartTrigger.class));
	}

	public GameStartTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.GAME_START;
	}

}
