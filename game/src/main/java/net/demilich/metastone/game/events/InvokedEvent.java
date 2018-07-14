package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.AbstractCardTrigger;

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
	public GameEventType getEventType() {
		return GameEventType.INVOKED;
	}

	@Override
	public Card getCard() {
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

