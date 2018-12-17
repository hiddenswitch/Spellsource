package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public final class AfterCardPlayedTrigger extends AbstractCardTrigger {

	private static final long serialVersionUID = -2268551034446172373L;

	public AfterCardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.AFTER_PLAY_CARD;
	}
}
