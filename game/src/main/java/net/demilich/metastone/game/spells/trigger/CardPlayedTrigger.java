package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardPlayedTrigger extends AbstractCardTrigger {

	private static final long serialVersionUID = -872548187047561470L;

	public CardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.PLAY_CARD;
	}

}

