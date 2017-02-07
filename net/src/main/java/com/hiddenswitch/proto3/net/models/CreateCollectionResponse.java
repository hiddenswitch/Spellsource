package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCollectionResponse implements Serializable {
	private String collectionId;

	protected CreateCollectionResponse() {}

	public static CreateCollectionResponse user(String userId) {
		return new CreateCollectionResponse()
				.withCollectionId(userId);
	}

	public static CreateCollectionResponse deck(String deckId) {
		return new CreateCollectionResponse()
				.withCollectionId(deckId);
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public CreateCollectionResponse withCollectionId(final String collectionId) {
		this.collectionId = collectionId;
		return this;
	}
}
