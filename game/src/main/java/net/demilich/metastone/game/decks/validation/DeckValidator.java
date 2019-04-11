package net.demilich.metastone.game.decks.validation;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;

/**
 * A function that determines whether the given card can be added to the given deck.
 */
@FunctionalInterface
public interface DeckValidator {

	boolean canAddCardToDeck(Card card, GameDeck deck);
}
