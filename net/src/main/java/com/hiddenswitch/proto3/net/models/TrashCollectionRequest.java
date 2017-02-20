package com.hiddenswitch.proto3.net.models;

/**
 * Created by bberman on 2/16/17.
 */
public class TrashCollectionRequest {
	private final String deckId;

	public TrashCollectionRequest(String deckId) {
		this.deckId = deckId;
	}

	public String getCollectionId() {
		return deckId;
	}
}
