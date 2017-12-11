package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public final class CardPlayedEvent extends GameEvent implements HasCard {

	private final Card card;

	public CardPlayedEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, -1);
		this.card = card;
	}

	@Override
	public Card getCard() {
		return card;
	}
	
	@Override
	public Entity getEventTarget() {
		return getCard();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.PLAY_CARD;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
