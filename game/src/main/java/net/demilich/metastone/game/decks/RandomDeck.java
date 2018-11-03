package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.decks.validation.DeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.toList;

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
	public RandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		super(heroClass);
		populate(deckFormat);
	}


	/**
	 * Creates a random deck with a random hero class and a balance of 50% Class cards and Neutral cards in the {@link
	 * DeckFormat#CUSTOM} format.
	 */
	public RandomDeck() {
		final List<HeroClass> baseHeroes = Arrays.stream(HeroClass.values()).filter(HeroClass::isBaseClass).collect(toList());
		final HeroClass randomHeroClass = baseHeroes.get(RandomUtils.nextInt(0, baseHeroes.size()));
		setHeroClass(randomHeroClass);
		populate(DeckFormat.CUSTOM);
	}

	void populate(DeckFormat deckFormat) {
		DeckValidator deckValidator = new DefaultDeckValidator();
		CardList classCards = CardCatalogue.query(deckFormat, card -> card.isCollectible()
				&& !card.getCardType().isCardType(CardType.HERO)
				&& !card.getCardType().isCardType(CardType.HERO_POWER)
				&& card.hasHeroClass(getHeroClass()));
		CardList neutralCards = CardCatalogue.query(deckFormat, card -> card.isCollectible()
				&& !card.getCardType().isCardType(CardType.HERO)
				&& !card.getCardType().isCardType(CardType.HERO_POWER)
				&& card.hasHeroClass(HeroClass.ANY));

		while (!this.isComplete()) {
			// random deck consists of roughly 50% class cards and 50% neutral
			// cards
			Card randomCard;
			if (classCards.isEmpty() && !neutralCards.isEmpty()) {
				randomCard = neutralCards.get(ThreadLocalRandom.current().nextInt(neutralCards.size()));
			} else if (classCards.isEmpty()) {
				break;
			} else {
				randomCard = ThreadLocalRandom.current().nextBoolean()
						? classCards.get(ThreadLocalRandom.current().nextInt(classCards.size()))
						: neutralCards.get(ThreadLocalRandom.current().nextInt(neutralCards.size()));
			}
			if (deckValidator.canAddCardToDeck(randomCard, this)) {
				this.getCards().addCard(randomCard.clone());
			}
		}

		setName("[Random deck]");
	}
}
