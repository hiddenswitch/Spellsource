package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class DeckDeleteResponse implements Serializable {
	private TrashCollectionResponse response;

	private DeckDeleteResponse() {
	}

	private DeckDeleteResponse(TrashCollectionResponse response) {
		this.response = response;
	}

	public static DeckDeleteResponse create(TrashCollectionResponse response) {
		return new DeckDeleteResponse(response);
	}

	public TrashCollectionResponse getResponse() {
		return response;
	}
}
