package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class ShuffledEvent extends GameEvent implements HasCard {

	private final Entity target;
	private final Card card;

	public ShuffledEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Entity target, Card card) {
		super(context, targetPlayerId, sourcePlayerId);
		this.target = target;
		this.card = card;
	}

	public ShuffledEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(context, targetPlayerId, sourcePlayerId);
		this.target = card;
		this.card = card;
	}

	@Override
	public Card getCard() {
		return card == null ? target.getSourceCard() : card;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.CARD_SHUFFLED;
	}
}

