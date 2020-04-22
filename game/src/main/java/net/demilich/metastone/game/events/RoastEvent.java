package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was roasted (discarded from the deck).
 * <p>
 * Roasts cannot be cancelled the same way discards can.
 */
public final class RoastEvent extends DiscardEvent {

	public RoastEvent(GameContext context, int playerId, Card card) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ROASTED, context, playerId, card);
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s roasted %s", context.getPlayer(playerId).getName(), getSourceCard().getName());
	}
}
