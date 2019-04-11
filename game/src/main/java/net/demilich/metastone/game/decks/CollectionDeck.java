package net.demilich.metastone.game.decks;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Indicates a deck from the player's online collection.
 */
public final class CollectionDeck implements Serializable, Cloneable, Deck {

	private String deckId;

	public CollectionDeck() {
	}

	public CollectionDeck(@NotNull String deckId) {
		this.deckId = deckId;
	}

	public CollectionDeck setDeckId(String deckId) {
		this.deckId = deckId;
		return this;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CollectionDeck that = (CollectionDeck) o;
		return Objects.equals(deckId, that.deckId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(deckId);
	}
}
