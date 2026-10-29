package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * A card was put into the hand.
 * <p>
 * {@link #isDrawn()} is {@code true} if the card was drawn from the deck.
 */
public final class DrawCardEvent extends CardEvent {

	private final boolean drawn;

	public DrawCardEvent(GameContext context, int playerId, Card card, boolean drawn) {
		this(context, playerId, card, null, drawn);
	}

	public DrawCardEvent(GameContext context, int playerId, Card card, Entity source, boolean drawn) {
		super(GameEventType.DRAW_CARD, context.getPlayer(playerId).hasAttribute(Attribute.STARTING_HAND_DRAWN) && drawn, context, source != null ? source : context.getPlayer(playerId), card, playerId, -1, card);
		this.drawn = drawn;
	}

	public boolean isDrawn() {
		return drawn;
	}
}
