package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardPlayedTrigger extends AbstractCardTrigger {

	public CardPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create() {
		return new EventTriggerDesc(CardPlayedTrigger.class);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.PLAY_CARD;
	}

}

