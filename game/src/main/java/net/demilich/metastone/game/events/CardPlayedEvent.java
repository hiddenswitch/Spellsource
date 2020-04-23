package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * The card is played from the hand. The Lun cost has been paid. The card is still in the {@link
 * net.demilich.metastone.game.targeting.Zones#HAND}.
 */
public final class CardPlayedEvent extends CardEvent implements HasCard {

	public CardPlayedEvent(GameContext context, int playerId, Card card) {
		super(EventTypeEnum.PLAY_CARD, true, context, playerId, -1, card);
	}
}
