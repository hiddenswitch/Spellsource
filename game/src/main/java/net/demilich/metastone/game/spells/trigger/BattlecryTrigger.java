package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BattlecryTrigger extends EventTrigger {
	private static final long serialVersionUID = 6430514874405024482L;

	public BattlecryTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return false;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_SUMMON;
	}
}
