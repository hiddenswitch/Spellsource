package net.demilich.metastone.game.decks;

import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.validation.DefaultDeckValidator;
import net.demilich.metastone.game.decks.validation.IDeckValidator;
import net.demilich.metastone.game.entities.heroes.HeroClass;

public class ProceduralRandomDeck extends Deck {
	protected CardList randomCards;

	public ProceduralRandomDeck(HeroClass heroClass) {
		super(heroClass);
		setName("[Procedural Random deck]");
		randomCards = createRandomCards();
	}

	@Override
	public CardList getCards() {
		return randomCards;
	}

	private CardList createRandomCards() {
		DeckFormat classDecks = new DeckFormat();
		classDecks.addSet(CardSet.BASIC);
		classDecks.addSet(CardSet.CLASSIC);
		classDecks.addSet(CardSet.PROCEDURAL_PREVIEW);

		DeckFormat proceduralDeck = new DeckFormat();
		proceduralDeck.addSet(CardSet.PROCEDURAL_PREVIEW);

		Deck copyDeck = new Deck(getHeroClass());
		IDeckValidator deckValidator = new DefaultDeckValidator();
		CardList classCards = CardCatalogue.query(classDecks, card -> {
			return card.isCollectible() && !card.getCardType().isCardType(CardType.HERO) && !card.getCardType().isCardType(CardType.HERO_POWER) && card.hasHeroClass(getHeroClass());
		});
		CardList neutralCards = CardCatalogue.query(proceduralDeck, card -> {
			return card.isCollectible() && !card.getCardType().isCardType(CardType.HERO) && !card.getCardType().isCardType(CardType.HERO_POWER) && card.hasHeroClass(HeroClass.ANY);
		});

		while (!copyDeck.isComplete()) {
			// random deck consists of roughly 50% class cards and 50% neutral
			// cards

			Card randomCard = ThreadLocalRandom.current().nextBoolean() ? classCards.getRandom() : neutralCards.getRandom();
			if (deckValidator.canAddCardToDeck(randomCard, copyDeck)) {
				copyDeck.getCards().addCard(randomCard);
			}
		}
		return copyDeck.getCardsCopy();
	}

}