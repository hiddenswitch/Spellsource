package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.CardLocation;

public class DrawCardEvent extends GameEvent {

	private final Card card;
	private final CardType sourceType;
	private final CardLocation from;
	private final boolean drawn;

	public DrawCardEvent(GameContext context, int playerId, Card card, CardType sourceType, CardLocation from, boolean drawn) {
		super(context, playerId, -1);
		this.card = card;
		this.sourceType = sourceType;
		this.from = from;
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

	public CardType getSourceType() {
		return sourceType;
	}

	public CardLocation getFrom() {
		return from;
	}

	public boolean isDrawn() {
		return drawn;
	}

}
