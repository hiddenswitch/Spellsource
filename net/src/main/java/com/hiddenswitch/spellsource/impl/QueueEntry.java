package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.util.DefaultClusterSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public final class QueueEntry implements Serializable, DefaultClusterSerializable {
	private GameId gameId;
	private DeckId deckId;

	private QueueEntry(GameId gameId, DeckId deckId) {
		this.gameId = gameId;
		this.deckId = deckId;
	}

	public static QueueEntry pending(DeckId deckId) {
		return new QueueEntry(GameId.PENDING, deckId);
	}

	public static QueueEntry ready(GameId gameId, DeckId deckId) {
		assert !gameId.equals(GameId.PENDING);
		return new QueueEntry(gameId, deckId);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QueueEntry)) {
			return false;
		}
		QueueEntry rhs = (QueueEntry) obj;
		return new EqualsBuilder()
				.append(gameId, rhs.gameId)
				.append(deckId, rhs.deckId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(gameId)
				.append(deckId)
				.toHashCode();
	}

	public GameId getGameId() {
		return gameId;
	}

	public DeckId getDeckId() {
		return deckId;
	}

	public boolean isPending() {
		return gameId.equals(GameId.PENDING);
	}
}
