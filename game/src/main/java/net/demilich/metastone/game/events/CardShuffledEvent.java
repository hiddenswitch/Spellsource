package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class CardShuffledEvent extends GameEvent implements HasCard {

	private final Card card;

	public CardShuffledEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(context, targetPlayerId, sourcePlayerId);
		this.card = card;
	}

	@Override
	public Card getCard() {
		return card;
	}

	@Override
	public Entity getEventTarget() {
		return card;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.CARD_SHUFFLED;
	}
}

