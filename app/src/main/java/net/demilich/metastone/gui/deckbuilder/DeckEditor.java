package net.demilich.metastone.gui.deckbuilder;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.validation.IDeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by bberman on 3/17/17.
 */
public interface DeckEditor {
	String NAME = "DeckProxy";

	boolean addCardToDeck(Card card);

	Deck getActiveDeck();

	List<Card> getCards(HeroClass heroClass);

	List<Deck> getDecks();

	void deleteDeck(Deck deck);

	void loadDecks() throws IOException, URISyntaxException;

	boolean nameAvailable(Deck deck);

	void removeCardFromDeck(Card card);

	void saveActiveDeck();

	void setActiveDeck(Deck activeDeck);

	void setActiveDeckValidator(IDeckValidator deckValidator);

	void setDeckName(String newDeckName);

	void createDeck(HeroClass heroClass);
}
