package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.DeckCreateRequest;

import java.io.Serializable;
import java.util.List;

public final class DeckListUpdateRequest implements Serializable {
	private List<DeckCreateRequest> deckCreateRequests;

	public DeckListUpdateRequest() {
	}

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
