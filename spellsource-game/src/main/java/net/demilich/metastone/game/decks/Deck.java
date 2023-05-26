package net.demilich.metastone.game.decks;

/**
 * A deck has, at minimum, a deck ID. The implementor {@link GameDeck} contains references to actual cards.
 */
public interface Deck extends Cloneable {

	static CollectionDeck forId(String id) {
		return new CollectionDeck().setDeckId(id);
	}

	String getDeckId();

	Deck clone();
}
