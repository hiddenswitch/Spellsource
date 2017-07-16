package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 2/19/17.
 */
public class LogicRequest implements Serializable {
	private String gameId;
	private String userId;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public LogicRequest withGameId(final String gameId) {
		this.gameId = gameId;
		return this;
	}

	public LogicRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

}
