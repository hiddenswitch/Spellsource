package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/22/17.
 */
public class GetCollectionRequest implements Serializable {
	private String userId;
	private String deckId;

	public GetCollectionRequest() {
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserId() {
		return userId;
	}

	public GetCollectionRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public GetCollectionRequest withDeckId(String deckId) {
		this.deckId = deckId;
		return this;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}

	public static GetCollectionRequest user(String userId) {
		return new GetCollectionRequest()
				.withUserId(userId);
	}

	public static GetCollectionRequest deck(String deckId) {
		return new GetCollectionRequest()
				.withDeckId(deckId);
	}
}


