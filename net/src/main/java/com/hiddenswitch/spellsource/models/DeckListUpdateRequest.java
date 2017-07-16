package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.List;

public class DeckListUpdateRequest implements Serializable {
	private List<DeckCreateRequest> deckCreateRequests;

	public List<DeckCreateRequest> getDeckCreateRequests() {
		return deckCreateRequests;
	}

	public void setDeckCreateRequests(List<DeckCreateRequest> deckCreateRequests) {
		this.deckCreateRequests = deckCreateRequests;
	}

	public DeckListUpdateRequest withDeckCreateRequests(final List<DeckCreateRequest> deckCreateRequests) {
		this.deckCreateRequests = deckCreateRequests;
		return this;
	}
}
