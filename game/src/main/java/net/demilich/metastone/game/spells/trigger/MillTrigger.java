package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class MillTrigger extends DiscardTrigger {

	private static final long serialVersionUID = 2200997775576980770L;

	public MillTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.MILL;
	}
}
