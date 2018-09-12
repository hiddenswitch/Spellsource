package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.entities.heroes.HeroClass;

public class DeckFactory {

	public static GameDeck getRandomDeck(HeroClass heroClass, DeckFormat deckFormat) {
		return new RandomDeck(heroClass, deckFormat);
	}

}
