package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class WillEndSequenceTrigger extends EventTrigger {
	private static final long serialVersionUID = -433968589362066912L;

	public WillEndSequenceTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public WillEndSequenceTrigger() {
		super(new EventTriggerDesc(WillEndSequenceTrigger.class));
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.WILL_END_SEQUENCE;
	}
}
