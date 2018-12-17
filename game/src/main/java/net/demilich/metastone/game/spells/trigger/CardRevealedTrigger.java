package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class CardRevealedTrigger extends AbstractCardTrigger {
	private static final long serialVersionUID = -4699571654790138389L;

	public CardRevealedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.REVEAL_CARD;
	}
}
