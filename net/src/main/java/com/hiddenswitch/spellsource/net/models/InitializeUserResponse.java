package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Indicates the result of initializing the specified user.
 */
public final class InitializeUserResponse implements Serializable {
	private CreateCollectionResponse createCollectionResponse;
	private List<DeckCreateResponse> deckCreateResponses;

	public InitializeUserResponse() {
	}

	public CreateCollectionResponse getCreateCollectionResponse() {
		return createCollectionResponse;
	}

	public void setCreateCollectionResponse(CreateCollectionResponse createCollectionResponse) {
		this.createCollectionResponse = createCollectionResponse;
	}

	public InitializeUserResponse withCreateCollectionResponse(final CreateCollectionResponse createCollectionResponse) {
		this.createCollectionResponse = createCollectionResponse;
		return this;
	}

	public List<DeckCreateResponse> getDeckCreateResponses() {
		return deckCreateResponses;
	}

	public void setDeckCreateResponses(List<DeckCreateResponse> deckCreateResponses) {
		this.deckCreateResponses = deckCreateResponses;
	}

	public InitializeUserResponse withDeckCreateResponses(final List<DeckCreateResponse> deckCreateResponses) {
		this.deckCreateResponses = deckCreateResponses;
		return this;
	}
}
