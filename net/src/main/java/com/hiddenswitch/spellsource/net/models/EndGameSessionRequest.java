package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;

/**
 * Indicates a request to end a specific game.
 */
public final class EndGameSessionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private String gameId;

	public EndGameSessionRequest() {
	}

	public EndGameSessionRequest(String gameId) {
		setGameId(gameId);
	}

	public EndGameSessionRequest withGameId(String gameId) {
		setGameId(gameId);
		return this;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
}
