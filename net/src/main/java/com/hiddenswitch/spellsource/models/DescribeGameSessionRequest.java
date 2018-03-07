package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 11/18/16.
 */
public final class DescribeGameSessionRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String gameId;

	private DescribeGameSessionRequest() {
	}

	private DescribeGameSessionRequest(String gameId) {
		setGameId(gameId);
	}

	public static DescribeGameSessionRequest create(String gameId) {
		return new DescribeGameSessionRequest(gameId);
	}

	public String getGameId() {
		return this.gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public DescribeGameSessionRequest withGameId(String gameId) {
		setGameId(gameId);
		return this;
	}
}
