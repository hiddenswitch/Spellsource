package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public final class CardPlayedEvent extends GameEvent implements HasCard {

	private final Card card;

	public CardPlayedEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, -1);
		this.card = card;
	}

	@Override
	public Card getSourceCard() {
		return card;
	}

	@Override
	public Entity getEventTarget() {
		return getSourceCard();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.PLAY_CARD;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
