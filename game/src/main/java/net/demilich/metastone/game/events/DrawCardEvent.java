package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was put into the hand.
 * <p>
 * {@link #isDrawn()} is {@code true} if the card was drawn from the deck.
 */
public final class DrawCardEvent extends CardEvent {

	private final boolean drawn;

	public DrawCardEvent(GameContext context, int playerId, Card card, boolean drawn) {
		super(EventTypeEnum.DRAW_CARD, context.getPlayer(playerId).hasAttribute(Attribute.STARTING_HAND_DRAWN) && drawn, context, playerId, -1, card);
		this.drawn = drawn;
	}

	public boolean isDrawn() {
		return drawn;
	}
}
