package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 3/21/17.
 */
public class ConcedeGameSessionRequest implements Serializable {
	private  String gameId;
	private  int playerId;

	private ConcedeGameSessionRequest() {
	}

	private ConcedeGameSessionRequest(String gameId, int playerId) {
		this.gameId = gameId;
		this.playerId = playerId;
	}

	public static ConcedeGameSessionRequest request(String gameId, int playerId) {
		return new ConcedeGameSessionRequest(gameId, playerId);
	}

	public String getGameId() {
		return gameId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
