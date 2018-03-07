package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.client.models.MatchmakingQueuePutResponseUnityConnection;

import java.io.Serializable;

/**
 * Created by bberman on 4/1/17.
 */
public final class CurrentMatchResponse implements Serializable {
	private String gameId;

	private CurrentMatchResponse() {
	}

	private CurrentMatchResponse(String gameId) {
		this.gameId = gameId;
	}

	public static CurrentMatchResponse response(String gameId) {
		return new CurrentMatchResponse(gameId);
	}

	public String getGameId() {
		return gameId;
	}
}
