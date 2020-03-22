package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Fired after a card is played.
 */
public class AfterCardPlayedEvent extends GameEvent implements HasCard {
	private Card card;

	public AfterCardPlayedEvent(GameContext context, int playerId, EntityReference cardReference) {
		super(context, playerId, playerId);
		card = (Card) context.resolveSingleTarget(cardReference);
	}

	@Override
	public Entity getEventTarget() {
		return null;
	}

	@Override
	public Entity getEventSource() {
		return card;
	}

	@Override
	public EventTypeEnum getEventType() {
		return EventTypeEnum.AFTER_PLAY_CARD;
	}

	@Override
	public Card getSourceCard() {
		return card;
	}
}
