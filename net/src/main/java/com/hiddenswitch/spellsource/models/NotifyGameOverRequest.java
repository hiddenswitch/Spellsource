package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/3/17.
 */
public class NotifyGameOverRequest implements Serializable {
	private final String gameId;

	public NotifyGameOverRequest(String gameId) {
		this.gameId = gameId;
	}

	public String getGameId() {
		return gameId;
	}
}
