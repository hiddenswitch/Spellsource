package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 12/8/16.
 */
public final class ContainsGameSessionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public String gameId;

	private ContainsGameSessionRequest() {
	}

	private ContainsGameSessionRequest(String gameId) {
		this.gameId = gameId;
	}

	public static ContainsGameSessionRequest request(String gameId) {
		return new ContainsGameSessionRequest(gameId);
	}
}
