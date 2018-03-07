package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class DeckDeleteRequest implements Serializable {
	private String deckId;

	private DeckDeleteRequest() {
	}

	private DeckDeleteRequest(String deckId) {
		this.deckId = deckId;
	}

	public static DeckDeleteRequest create(String deckId) {
		return new DeckDeleteRequest(deckId);
	}

	public String getDeckId() {
		return deckId;
	}
}
