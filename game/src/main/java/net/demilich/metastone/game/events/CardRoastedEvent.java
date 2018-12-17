package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class CardRoastedEvent extends GameEvent implements HasCard {
	private static final long serialVersionUID = 8655207013683400518L;
	private Card card;

	public CardRoastedEvent(GameContext context, Card card) {
		super(context, card.getOwner(), card.getOwner());
		this.card = card;
	}

	@Override
	public Entity getEventTarget() {
		return card;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.ROASTED;
	}

	@Override
	public Card getCard() {
		return card;
	}
}
