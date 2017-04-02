package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/1/17.
 */
public class CurrentMatchResponse implements Serializable {
	private String gameId;

	public CurrentMatchResponse(String gameId) {
		this.gameId = gameId;
	}

	public String getGameId() {
		return gameId;
	}
}
