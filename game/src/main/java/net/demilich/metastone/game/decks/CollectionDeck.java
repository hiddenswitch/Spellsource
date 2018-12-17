package net.demilich.metastone.game.decks;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Indicates a deck from the player's online collection.
 */
public final class CollectionDeck implements Serializable, Cloneable, Deck {

	private static final long serialVersionUID = -2966752971975044125L;
	private final String deckId;

	public CollectionDeck(@NotNull String deckId) {
		this.deckId = deckId;
	}

	@Override
	public String getDeckId() {
		return this.deckId;
	}

	@Override
	public CollectionDeck clone() {
		try {
			return (CollectionDeck) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		Deck rhs = (Deck) obj;
		if (rhs == null) {
			return false;
		}

		return this.getDeckId().equals(rhs.getDeckId());
	}

	@Override
	public int hashCode() {
		return deckId.hashCode();
	}
}
