package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.client.models.MatchmakingQueuePutResponseUnityConnection;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;

import java.io.Serializable;

public class MatchmakingResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private MatchmakingRequest retry;
	private String gameId;

	public MatchmakingRequest getRetry() {
		return retry;
	}

	public void setRetry(MatchmakingRequest retry) {
		this.retry = retry;
	}

	public MatchmakingResponse withRetry(MatchmakingRequest matchmakingRequest) {
		this.retry = matchmakingRequest;
		return this;
	}

	public static MatchmakingResponse notReady(String userId, String deckId) {
		return new MatchmakingResponse()
				.withRetry(new MatchmakingRequest()
						.withUserId(userId)
						.withDeckId(deckId));
	}

	public static MatchmakingResponse ready(String gameId) {
		return new MatchmakingResponse()
				.withRetry(null)
				.withGameId(gameId);
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public MatchmakingResponse withGameId(final String gameId) {
		this.gameId = gameId;
		return this;
	}
}
