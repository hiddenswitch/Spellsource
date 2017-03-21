package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class MatchCancelResponse implements Serializable {
	private static final long serialVersionUID = 2L;

	private final boolean isCanceled;
	private final String gameId;
	private final int playerId;

	public MatchCancelResponse(boolean value, String gameId, int playerId) {
		this.isCanceled = value;
		this.gameId = gameId;
		this.playerId = playerId;
	}

	public boolean getCanceled() {
		return isCanceled;
	}

	public String getGameId() {
		return gameId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
