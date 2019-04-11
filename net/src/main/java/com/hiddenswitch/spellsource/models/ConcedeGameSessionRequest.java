package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;

/**
 * Indicates a request to concede a game session.
 */
public class ConcedeGameSessionRequest implements Serializable {
	private String gameId;
	private int playerId;
	private UserId userId;

	private ConcedeGameSessionRequest() {
	}

	private ConcedeGameSessionRequest(String gameId, int playerId) {
		this.gameId = gameId;
		this.playerId = playerId;
	}

	public static ConcedeGameSessionRequest request(String gameId, int playerId) {
		return new ConcedeGameSessionRequest(gameId, playerId);
	}
	public static ConcedeGameSessionRequest request(GameId gameId, UserId userId) {
		ConcedeGameSessionRequest req = new ConcedeGameSessionRequest();
		req.gameId = gameId.toString();
		req.userId = userId;
		return req;
	}

	public String getGameId() {
		return gameId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public UserId getUserId() {
		return userId;
	}

	public ConcedeGameSessionRequest setGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	public ConcedeGameSessionRequest setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public ConcedeGameSessionRequest setUserId(UserId userId) {
		this.userId = userId;
		return this;
	}
}
