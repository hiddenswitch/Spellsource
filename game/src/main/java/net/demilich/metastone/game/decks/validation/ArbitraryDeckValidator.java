package net.demilich.metastone.game.decks.validation;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * A deck validator that only cares that the deck contain no more than {@link GameLogic#MAX_DECK_SIZE} many cards.
 */
public final class ArbitraryDeckValidator implements DeckValidator {

	@Override
	public boolean canAddCardToDeck(Card card, GameDeck deck) {
		return deck.getCards().getCount() <= GameLogic.MAX_DECK_SIZE;
	}

}
