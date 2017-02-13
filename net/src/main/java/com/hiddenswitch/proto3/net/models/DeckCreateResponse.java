package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class DeckCreateResponse implements Serializable {
	private final String collectionId;

	public DeckCreateResponse(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionId() {
		return collectionId;
	}
}
