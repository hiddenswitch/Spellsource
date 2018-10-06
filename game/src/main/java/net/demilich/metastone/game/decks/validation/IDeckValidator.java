package net.demilich.metastone.game.decks.validation;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;

public interface IDeckValidator {

	boolean canAddCardToDeck(Card card, GameDeck deck);

}
