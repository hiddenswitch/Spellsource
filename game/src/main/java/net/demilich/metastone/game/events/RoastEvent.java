package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

public final class RoastEvent extends DiscardEvent {
	public RoastEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, card);
	}

	@Override
	public GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.ROASTED;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s roasted %s", context.getPlayer(playerId).getName(), getSourceCard().getName());
	}
}
