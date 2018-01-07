package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;

import java.io.Serializable;

/**
 * Created by bberman on 4/2/17.
 */
public class BotsStartGameResponse implements Serializable {
	private String gameId;
	private String botUserId;
	private String botDeckId;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getBotUserId() {
		return botUserId;
	}

	public void setBotUserId(String botUserId) {
		this.botUserId = botUserId;
	}

	public String getBotDeckId() {
		return botDeckId;
	}

	public void setBotDeckId(String botDeckId) {
		this.botDeckId = botDeckId;
	}
}
