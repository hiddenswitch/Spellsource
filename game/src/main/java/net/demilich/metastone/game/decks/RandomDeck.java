package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.decks.validation.DeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A deck that was randomly created.
 */
final class RandomDeck extends GameDeck {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a random deck, 50% Class cards and 50% Neutrals on average, with the specified hero class and format.
	 *
	 * @param heroClass  The hero class
	 * @param deckFormat The format
	 */
	RandomDeck(String heroClass, DeckFormat deckFormat) {
		super(heroClass);
		populate(deckFormat);
	}

	private void populate(DeckFormat deckFormat) {
		DeckValidator deckValidator = new DefaultDeckValidator();
		CardList classCards = CardCatalogue.query(deckFormat, card -> card.isCollectible()
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO)
				&& !GameLogic.isCardType(card.getCardType(), CardType.HERO_POWER)
				&& !GameLogic.isCardType(card.getCardType(), CardType.CLASS)
				&& !GameLogic.isCardType(card.getCardType(), CardType.FORMAT)
				&& card.hasHeroClass(getHeroClass()));
		CardList neutralCards = CardCatalogue.query(deckFormat, card -> card.isCollectible()
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
