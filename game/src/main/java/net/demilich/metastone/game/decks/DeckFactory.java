package net.demilich.metastone.game.decks;

import java.util.concurrent.ThreadLocalRandom;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomUtils;

public class DeckFactory {

	public static Deck getDeckConsistingof(int count, Card... cards) {
		CardList cardList = new CardArrayList();
		for (int i = 0; i < count; i++) {
			int randomIndex = ThreadLocalRandom.current().nextInt(cards.length);
			cardList.addCard(cards[randomIndex].clone());
		}
		Deck deck = new Deck(HeroClass.ANY);
		deck.setName("[Debug deck]");
		deck.getCards().addAll(cardList);
		return deck;
	}

	public static Deck getRandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

	public static Deck getRandomDeck() {
		HeroClass[] heroClasses = {HeroClass.BROWN, HeroClass.GREEN, HeroClass.BLUE, HeroClass.GOLD, HeroClass.WHITE, HeroClass.BLACK, HeroClass.SILVER, HeroClass.VIOLET, HeroClass.RED};
		Deck randomDeck = DeckFactory.getRandomDeck(
				heroClasses[RandomUtils.nextInt(0, heroClasses.length)],
				new DeckFormat().withCardSets(
						CardSet.BASIC,
						CardSet.CLASSIC));
		return randomDeck;
	}
}
