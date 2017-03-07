package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class DeckCreateResponse implements Serializable {
	private final String deckId;

	public DeckCreateResponse(String deckId) {
		this.deckId = deckId;
	}

	public String getDeckId() {
		return deckId;
	}
}
