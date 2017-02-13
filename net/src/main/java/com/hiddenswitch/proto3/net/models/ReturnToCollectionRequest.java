package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class ReturnToCollectionRequest implements Serializable {
	private String deckId;
	private List<String> userIds;
	private List<String> deckIds;

	public ReturnToCollectionRequest withCollectionId(String deckId) {
		this.deckId = deckId;
		return this;
	}

	public ReturnToCollectionRequest withUserIds(List<String> userIds) {
		this.userIds = userIds;
		return this;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public ReturnToCollectionRequest withDeckIds(List<String> deckIds) {
		this.deckIds = deckIds;
		return this;
	}

	public List<String> getDeckIds() {
		return deckIds;
	}

	public void setDeckIds(List<String> deckIds) {
		this.deckIds = deckIds;
	}
}
