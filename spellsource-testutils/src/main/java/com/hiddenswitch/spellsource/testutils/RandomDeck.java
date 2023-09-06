package com.hiddenswitch.spellsource.testutils;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.decks.validation.DeckValidator;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A deck that was randomly created.
 */
public final class RandomDeck extends GameDeck {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a random deck, 50% Class cards and 50% Neutrals on average, with the specified hero class and format.
	 *
	 * @param heroClass  The hero class
	 * @param deckFormat The format
	 */
	public RandomDeck(String heroClass, DeckFormat deckFormat) {
		this(ThreadLocalRandom.current().nextLong(), heroClass, deckFormat);
	}

	public RandomDeck(long seed, String heroClass, DeckFormat deckFormat) {
		this(seed, heroClass, deckFormat, ClasspathCardCatalogue.INSTANCE);
	}

	public RandomDeck(long seed, String heroClass, DeckFormat deckFormat, CardCatalogue cardCatalogue) {
		super(heroClass);
		populate(seed, deckFormat, cardCatalogue);
	}

	/**
	 * Creates a random deck with the given hero class and deck format.
	 * <p>
	 * The random deck creation function tries to make a balance of 50% class cards and 50% neutrals.
	 *
	 * @param heroClass  A hero class that is a base class
	 * @param deckFormat A deck format, like {@link CardCatalogue#spellsource()}.
	 * @return
	 */
	public static @NotNull
	GameDeck randomDeck(@NotNull String heroClass, @NotNull DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

	public static @NotNull
	GameDeck randomDeck(@NotNull DeckFormat deckFormat) {
		return new RandomDeck(TestBase.randomHeroCard(deckFormat), deckFormat);
	}

	public static @NotNull GameDeck randomDeck(long seed, CardCatalogue cardCatalogue) {
		var random = new XORShiftRandom(seed);
		DeckFormat deckFormat = cardCatalogue.spellsource();
		var baseClasses = cardCatalogue.getBaseClasses(deckFormat);
		var heroClass = new ArrayList<>(baseClasses).get(random.nextInt(baseClasses.size()));
		return new RandomDeck(random.getState(), heroClass, cardCatalogue.spellsource());
	}

	public static @NotNull GameDeck randomDeck(DeckFormat deckFormat, CardCatalogue cardCatalogue) {
		return new RandomDeck(ThreadLocalRandom.current().nextLong(), TestBase.randomHeroCard(deckFormat, cardCatalogue), deckFormat, cardCatalogue);
	}

	private void populate(long seed, DeckFormat deckFormat, CardCatalogue cardCatalogue) {
		var random = new XORShiftRandom(seed);
		DeckValidator deckValidator = new DefaultDeckValidator();
		var classCards = cardCatalogue.queryClassCards(deckFormat, getHeroClass());
		var neutralCards = cardCatalogue.queryNeutrals(deckFormat);

		while (!this.isComplete()) {
			// random deck consists of roughly 50% class cards and 50% neutral
			// cards
			Card randomCard;
			if (classCards.isEmpty() && !neutralCards.isEmpty()) {
				randomCard = neutralCards.get(random.nextInt(neutralCards.size()));
			} else if (classCards.isEmpty()) {
				break;
			} else {
				randomCard = random.nextBoolean()
						? classCards.get(random.nextInt(classCards.size()))
						: neutralCards.get(random.nextInt(neutralCards.size()));
			}
			if (deckValidator.canAddCardToDeck(randomCard, this)) {
				this.getCards().addCard(randomCard.clone());
			}
		}

		setName("[Random deck]");
	}
}
