package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class OverloadEvent extends GameEvent implements HasValue {
	private Card card;
	private int manaCrystalsOverloaded;

	public OverloadEvent(GameContext context, int playerId, Card card, int manaCrystalsOverloaded) {
		super(context, playerId, playerId);
		this.manaCrystalsOverloaded = manaCrystalsOverloaded;
		this.card = card;
	}

	public Card getCard() {
		return card;
	}

	@Override
	public Entity getEventTarget() {
		return getCard();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.OVERLOAD;
	}

	@Override
	public int getValue() {
		return manaCrystalsOverloaded;
	}
}
