package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 3/21/17.
 */
public class ConcedeGameSessionRequest implements Serializable {
	private final String gameId;
	private final int playerId;

	public ConcedeGameSessionRequest(String gameId, int playerId) {
		this.gameId = gameId;
		this.playerId = playerId;
	}

	public String getGameId() {
		return gameId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
