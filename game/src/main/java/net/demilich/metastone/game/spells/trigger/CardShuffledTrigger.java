package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardShuffledTrigger extends AbstractCardTrigger {

	public CardShuffledTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.CARD_SHUFFLED;
	}
}
