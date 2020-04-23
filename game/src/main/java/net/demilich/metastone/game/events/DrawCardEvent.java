package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A card was put into the hand.
 * <p>
 * {@link #isDrawn()} is {@code true} if the card was drawn from the deck.
 */
public final class DrawCardEvent extends CardEvent {

	private final boolean drawn;

	public DrawCardEvent(GameContext context, int playerId, Card card, boolean drawn) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DRAW_CARD, context, playerId, -1, card);
		this.drawn = drawn;
	}

	public boolean isDrawn() {
		return drawn;
	}
}
