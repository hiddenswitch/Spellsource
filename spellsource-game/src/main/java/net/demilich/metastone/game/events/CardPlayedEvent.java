package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.HasCard;

/**
 * The card is played from the hand. The Lun cost has been paid. The card is still in the {@link
 * com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#HAND}.
 */
public final class CardPlayedEvent extends CardEvent implements HasCard {

	public CardPlayedEvent(GameContext context, int playerId, Card card) {
		super(GameEventType.PLAY_CARD, true, context, playerId, -1, card);
	}
}
