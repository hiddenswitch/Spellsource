package net.demilich.metastone.game.decks;

public interface Deck extends Cloneable {
	String getDeckId();

	Deck clone();
}
