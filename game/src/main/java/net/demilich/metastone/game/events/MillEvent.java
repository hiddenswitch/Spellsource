package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

public final class MillEvent extends DiscardEvent {
	public MillEvent(GameContext context, int id, Card card) {
		super(context, id, card);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.MILL;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
