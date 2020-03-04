package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class InvokedEvent extends GameEvent implements HasValue, HasCard {
	private final Card card;
	private final int invokedMana;

	public InvokedEvent(GameContext context, int playerId, Card card, int invokedMana) {
		super(context, playerId, playerId);
		this.card = card;
		this.invokedMana = invokedMana;
	}

	@Override
	public Entity getEventTarget() {
		return card;
	}

	@Override
	public Entity getEventSource() {
		return card;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.INVOKED;
	}

	@Override
	public Card getSourceCard() {
		return card;
	}

	@Override
	public int getValue() {
		return invokedMana;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}

