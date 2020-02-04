package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;

public final class DrawCardEvent extends GameEvent implements HasCard {

	private final Card card;
	private final boolean drawn;

	public DrawCardEvent(GameContext context, int playerId, Card card, boolean drawn) {
		super(context, playerId, -1);
		this.card = card;
		this.drawn = drawn;
	}

	public Card getCard() {
		return card;
	}

	@Override
	public Entity getEventTarget() {
		return card;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.DRAW_CARD;
	}

	public boolean isDrawn() {
		return drawn;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
