package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;

/**
 * A deck has, at minimum, a deck ID. The implementor {@link GameDeck} contains references to actual cards.
 */
public interface Deck extends Cloneable {
	/**
	 * Creates a random deck with the given hero class and deck format.
	 * <p>
	 * The random deck creation function tries to make a balance of 50% class cards and 50% neutrals.
	 *
	 * @param heroClass  A hero class that is a base class
	 * @param deckFormat A deck format, like {@link DeckFormat#STANDARD}.
	 * @return
	 */
	static @NotNull
	GameDeck randomDeck(@NotNull String heroClass, @NotNull DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

	static @NotNull
	GameDeck randomDeck(@NotNull DeckFormat deckFormat) {
		return new RandomDeck(HeroClass.random(deckFormat), deckFormat);
	}

	static @NotNull
	GameDeck randomDeck(@NotNull String heroClass) {
		return new RandomDeck(heroClass, DeckFormat.getFormat("Custom"));
	}

	static @NotNull
	GameDeck randomDeck() {
		return new RandomDeck(HeroClass.random(DeckFormat.getFormat("Custom")), DeckFormat.getFormat("Custom"));
	}

	/**
	 * Retrieves a deck from a deck string, either in a community format or using a deck string.
	 *
	 * @param deckList
	 * @return
	 */
	static @NotNull
	GameDeck deckList(@NotNull String deckList) {
		return DeckCreateRequest.fromDeckList(deckList).toGameDeck();
	}

	String getDeckId();

	Deck clone();
}
