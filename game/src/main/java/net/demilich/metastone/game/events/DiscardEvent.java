package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A card was discarded.
 * <p>
 * At the moment this event is fired, the card is still in the {@link net.demilich.metastone.game.targeting.Zones#HAND}.
 * The discard can be cancelled by removing the {@link net.demilich.metastone.game.cards.Attribute#DISCARDED} attribute
 * from the {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}.
 */
public class DiscardEvent extends CardEvent {

	public DiscardEvent(GameContext context, int playerId, Card card) {
		super(EventTypeEnum.DISCARD, true, context, playerId, -1, card);
	}

	DiscardEvent(EventTypeEnum eventTypeEnum, GameContext context, int playerId, Card card) {
		super(eventTypeEnum, true, context, playerId, -1, card);
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
