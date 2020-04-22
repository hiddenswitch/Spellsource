package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

/**
 * A card was inserted or shuffled into the deck, including "extra copies."
 * <p>
 * Some effects quietly add cards to the deck and do not fire this event, like {@link
 * net.demilich.metastone.game.spells.ResetDeckSpell}.
 */
public class CardAddedToDeckEvent extends CardEvent {

	public CardAddedToDeckEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(GameEvent.EventTypeEnum.CARD_ADDED_TO_DECK, context, targetPlayerId, sourcePlayerId, card);
	}
}
