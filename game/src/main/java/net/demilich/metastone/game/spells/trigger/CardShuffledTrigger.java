package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardShuffledTrigger extends AbstractCardTrigger {

	private static final long serialVersionUID = 1567471972199999006L;

	public CardShuffledTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.CARD_SHUFFLED;
	}
}
