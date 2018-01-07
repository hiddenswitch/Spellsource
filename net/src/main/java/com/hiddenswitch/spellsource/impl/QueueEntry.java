package com.hiddenswitch.spellsource.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiddenswitch.spellsource.util.DefaultClusterSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public final class QueueEntry implements Serializable, DefaultClusterSerializable {
	public GameId gameId;
	public DeckId deckId;

	public QueueEntry() {
	}

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

	@JsonIgnore
	public boolean isPending() {
		return gameId.equals(GameId.PENDING);
	}
}
