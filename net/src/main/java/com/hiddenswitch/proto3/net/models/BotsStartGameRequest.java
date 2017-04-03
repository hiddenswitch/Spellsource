package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.util.QueueEntry;

import java.io.Serializable;

/**
 * Created by bberman on 4/2/17.
 */
public class BotsStartGameRequest implements Serializable {

	private String userId;
	private String deckId;

	public BotsStartGameRequest(String userId, String deckId) {
		this.userId = userId;
		this.deckId = deckId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}
}
