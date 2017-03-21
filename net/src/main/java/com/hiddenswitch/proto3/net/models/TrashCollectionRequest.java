package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 2/16/17.
 */
public class TrashCollectionRequest implements Serializable {
	private final String deckId;

	public TrashCollectionRequest(String deckId) {
		this.deckId = deckId;
	}

	public String getCollectionId() {
		return deckId;
	}
}
