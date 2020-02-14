package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class CardAddedToDeckEvent extends GameEvent implements HasCard {

	private final Card card;

	public CardAddedToDeckEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(context, targetPlayerId, sourcePlayerId);
		this.card = card;
	}

	@Override
	public Entity getEventTarget() {
		return card;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.CARD_ADDED_TO_DECK;
	}

	@Override
	public Card getSourceCard() {
		return card;
	}
}
