package com.hiddenswitch.spellsource.testutils;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.decks.validation.DeckValidator;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;

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
		super(heroClass);
		populate(seed, deckFormat);
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

	public static @NotNull GameDeck randomDeck(long seed) {
		var random = new XORShiftRandom(seed);
        DeckFormat deckFormat = ClasspathCardCatalogue.CLASSPATH.spellsource();
        var baseClasses = ClasspathCardCatalogue.CLASSPATH.getBaseClasses(deckFormat);
		var heroClass = baseClasses.get(random.nextInt(baseClasses.size()));
        return new RandomDeck(random.getState(), heroClass, ClasspathCardCatalogue.CLASSPATH.spellsource());
	}

	private void populate(long seed, DeckFormat deckFormat) {
		var random = new XORShiftRandom(seed);
		DeckValidator deckValidator = new DefaultDeckValidator();
        CardList classCards = ClasspathCardCatalogue.CLASSPATH.query(deckFormat, card -> card.isCollectible()
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO)
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO_POWER)
				&& !GameLogic.isCardType(card.getCardType(), CardType.CLASS)
				&& !GameLogic.isCardType(card.getCardType(), CardType.FORMAT)
				&& card.hasHeroClass(getHeroClass()));
        CardList neutralCards = ClasspathCardCatalogue.CLASSPATH.query(deckFormat, card -> card.isCollectible()
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO)
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO_POWER)
				&& !GameLogic.isCardType(card.getCardType(), CardType.CLASS)
				&& !GameLogic.isCardType(card.getCardType(), CardType.FORMAT)
				&& card.hasHeroClass(HeroClass.ANY));

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
