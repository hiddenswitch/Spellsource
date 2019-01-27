package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

public final class RoastEvent extends DiscardEvent {
	public RoastEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, card);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.ROASTED;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s roasted %s", context.getPlayer(playerId).getName(), getCard().getName());
	}
}
