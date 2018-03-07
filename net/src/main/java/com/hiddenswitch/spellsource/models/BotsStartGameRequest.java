package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/2/17.
 */
public final class BotsStartGameRequest implements Serializable {

	private String userId;
	private String deckId;

	private BotsStartGameRequest() {
	}

	private BotsStartGameRequest(String userId, String deckId) {
		this.userId = userId;
		this.deckId = deckId;
	}

	public static BotsStartGameRequest request(String userId, String deckId) {
		return new BotsStartGameRequest(userId, deckId);
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
