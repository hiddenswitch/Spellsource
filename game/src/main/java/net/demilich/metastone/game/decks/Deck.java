package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.entities.heroes.HeroClass;

/**
 * A deck has, at minimum, a deck ID. The implementor {@link GameDeck} contains references to actual cards.
 */
public interface Deck extends Cloneable {
	/**
	 * Retrieves a random deck with the given hero class and deck format.
	 * <p>
	 * The random deck creation function tries to make a balance of 50% class cards and 50% neutrals.
	 *
	 * @param heroClass  A hero class that is {@link HeroClass#isBaseClass()}.
	 * @param deckFormat A deck format, like {@link DeckFormat#STANDARD}.
	 * @return
	 */
	static GameDeck getRandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

	String getDeckId();

	Deck clone();
}
