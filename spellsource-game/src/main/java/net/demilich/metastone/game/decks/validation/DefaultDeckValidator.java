package net.demilich.metastone.game.decks.validation;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * A deck validator of standard deck rules.
 * <p>
 * It limits the number of duplicates of legendaries to 1, duplicates of other cards to 2.
 * <p>
 * It requires exactly {@link GameLogic#DECK_SIZE} cards.
 * <p>
 * It currently permits cards of any class regardless of the hero class of the given deck.
 * <p>
 * The multiplayer Spellsource code contains different logic for validating decks.
 */
public final class DefaultDeckValidator implements DeckValidator {

	@Override
	public boolean canAddCardToDeck(Card card, GameDeck deck) {
		int maxDeckSize = deck.getDeckSize();
		if (card.hasAttribute(Attribute.DECK_SIZE)) {
			maxDeckSize = Math.max(maxDeckSize, card.getAttributeValue(Attribute.DECK_SIZE));
		}
		if (deck.getCards().getCount() > maxDeckSize) {
			return false;
		}
		int cardInDeckCount = deck.containsHowMany(card);
		return card.getRarity() == Rarity.LEGENDARY ? cardInDeckCount < 1 : cardInDeckCount < 2;
	}

}
