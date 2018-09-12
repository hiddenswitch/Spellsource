package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;

public class DeckFactory {

	public static GameDeck getRandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

}
