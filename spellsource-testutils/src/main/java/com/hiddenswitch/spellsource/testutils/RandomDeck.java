package com.hiddenswitch.spellsource.testutils;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.decks.validation.DeckValidator;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import net.demilich.metastone.tests.util.TestBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A deck that was randomly created.
 */
public final class RandomDeck extends GameDeck {
	private static final long serialVersionUID = 1L;
	private static final ConcurrentMap<CandidatePoolKey, List<Card>> CLASS_CARD_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentMap<CandidatePoolKey, List<Card>> NEUTRAL_CARD_CACHE = new ConcurrentHashMap<>();

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
		var classCards = cachedClassCards(cardCatalogue, deckFormat, getHeroClass());
		var neutralCards = cachedNeutralCards(cardCatalogue, deckFormat);

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

	private static List<Card> cachedClassCards(CardCatalogue cardCatalogue, DeckFormat deckFormat, String heroClass) {
		return CLASS_CARD_CACHE.computeIfAbsent(new CandidatePoolKey(cardCatalogue, deckFormat, heroClass), key ->
				Collections.unmodifiableList(loadDeckCandidates(key.cardCatalogue, key.deckFormat, key.heroClass, false)));
	}

	private static List<Card> cachedNeutralCards(CardCatalogue cardCatalogue, DeckFormat deckFormat) {
		return NEUTRAL_CARD_CACHE.computeIfAbsent(new CandidatePoolKey(cardCatalogue, deckFormat, HeroClass.ANY), key ->
				Collections.unmodifiableList(loadDeckCandidates(key.cardCatalogue, key.deckFormat, HeroClass.ANY, true)));
	}

	private static List<Card> loadDeckCandidates(CardCatalogue cardCatalogue, DeckFormat deckFormat, String heroClass, boolean neutralOnly) {
		var result = new ArrayList<Card>();
		var catalogueCards = new ArrayList<>(cardCatalogue.getCards().values());
		for (var card : catalogueCards) {
			var cardType = card.getCardType();
			if (!deckFormat.isInFormat(card)
					|| !card.isCollectible()
					|| card.isQuest()
					|| card.hasAttribute(Attribute.PERMANENT)
					|| cardType == com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType.HERO_POWER
					|| cardType == com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType.CLASS
					|| cardType == com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType.FORMAT) {
				continue;
			}

			boolean heroMatches = neutralOnly ? card.hasHeroClass(HeroClass.ANY) : card.hasHeroClass(heroClass);
			if (!heroMatches) {
				continue;
			}

			result.add(card.getCopy());
		}
		return result;
	}

	private record CandidatePoolKey(CardCatalogue cardCatalogue, DeckFormat deckFormat, String heroClass) {
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof CandidatePoolKey that)) {
				return false;
			}
			return cardCatalogue == that.cardCatalogue
					&& Objects.equals(deckFormat, that.deckFormat)
					&& Objects.equals(heroClass, that.heroClass);
		}

		@Override
		public int hashCode() {
			return 31 * System.identityHashCode(cardCatalogue) + Objects.hash(deckFormat, heroClass);
		}
	}
}
