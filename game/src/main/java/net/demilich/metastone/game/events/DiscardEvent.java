package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class DiscardEvent extends GameEvent implements HasCard {
	private final Card card;

	public DiscardEvent(GameContext context, int playerId, Card card) {
		super(context, playerId, -1);
		this.card = card;
	}

	public Card getSourceCard() {
		return card;
	}

	@Override
	public Entity getEventTarget() {
		return getSourceCard();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DISCARD;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s discarded %s", context.getPlayer(playerId).getName(), getSourceCard().getName());
	}
}
