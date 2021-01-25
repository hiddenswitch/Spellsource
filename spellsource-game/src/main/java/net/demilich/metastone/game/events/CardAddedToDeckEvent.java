package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;

/**
 * A card was inserted or shuffled into the deck, including "extra copies."
 * <p>
 * Some effects quietly add cards to the deck and do not fire this event, like {@link
 * net.demilich.metastone.game.spells.ResetDeckSpell}.
 */
public class CardAddedToDeckEvent extends CardEvent {

	public CardAddedToDeckEvent(GameContext context, int targetPlayerId, int sourcePlayerId, Card card) {
		super(GameEventType.CARD_ADDED_TO_DECK, context, targetPlayerId, sourcePlayerId, card);
	}
}
