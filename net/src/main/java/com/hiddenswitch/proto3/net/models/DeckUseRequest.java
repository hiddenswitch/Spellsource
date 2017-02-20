package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 2/4/17.
 */
public class DeckUseRequest implements Serializable {
	private String deckId;
	private DeckInstanceType deckInstanceType;
	private String userId;

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}

	public DeckInstanceType getDeckInstanceType() {
		return deckInstanceType;
	}

	public void setDeckInstanceType(DeckInstanceType deckInstanceType) {
		this.deckInstanceType = deckInstanceType;
	}

	public DeckUseRequest withDeckId(String deckId) {
		this.deckId = deckId;
		return this;
	}

	public DeckUseRequest withDeckInstanceType(DeckInstanceType deckInstanceType) {
		this.deckInstanceType = deckInstanceType;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
