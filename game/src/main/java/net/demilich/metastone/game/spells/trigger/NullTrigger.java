package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class NullTrigger extends EventTrigger {

	public NullTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create() {
		return new EventTriggerDesc(NullTrigger.class);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return false;
	}

	@Override
	public GameEventType interestedIn() {
		return null;
	}
}
