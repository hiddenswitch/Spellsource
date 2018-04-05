package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/2/17.
 */
public final class BotsStartGameRequest implements Serializable {

	private String userId;
	private String deckId;
	private String botDeckId;

	private BotsStartGameRequest() {
	}

	public static BotsStartGameRequest request(String userId, String deckId, String botDeckId) {
		BotsStartGameRequest request = new BotsStartGameRequest();
		request.userId = userId;
		request.deckId = deckId;
		request.botDeckId = botDeckId;
		return request;
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

	public String getBotDeckId() {
		return botDeckId;
	}

	public void setBotDeckId(String botDeckId) {
		this.botDeckId = botDeckId;
	}
}
