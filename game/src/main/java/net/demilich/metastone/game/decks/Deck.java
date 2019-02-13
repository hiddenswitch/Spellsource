package net.demilich.metastone.game.decks;

import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A deck has, at minimum, a deck ID. The implementor {@link GameDeck} contains references to actual cards.
 */
public interface Deck extends Cloneable {
	/**
	 * Creates a random deck with the given hero class and deck format.
	 * <p>
	 * The random deck creation function tries to make a balance of 50% class cards and 50% neutrals.
	 *
	 * @param heroClass  A hero class that is {@link HeroClass#isBaseClass()}.
	 * @param deckFormat A deck format, like {@link DeckFormat#STANDARD}.
	 * @return
	 */
	static @NotNull
	GameDeck randomDeck(@NotNull HeroClass heroClass, @NotNull DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

	static @NotNull
	GameDeck randomDeck(@NotNull DeckFormat deckFormat) {
		return new RandomDeck(HeroClass.random(), deckFormat);
	}

	static @NotNull
	GameDeck randomDeck(@NotNull HeroClass heroClass) {
		return new RandomDeck(heroClass, DeckFormat.CUSTOM);
	}

	static @NotNull
	GameDeck randomDeck() {
		List<HeroClass> baseClasses = HeroClass.getBaseClasses();
		return new RandomDeck(HeroClass.random(), DeckFormat.CUSTOM);
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
