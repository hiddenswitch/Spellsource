package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/1/17.
 */
public final class CurrentMatchRequest implements Serializable {
	private String userId;

	private CurrentMatchRequest() {
	}

	private CurrentMatchRequest(String userId) {
		this.userId = userId;
	}

	public static CurrentMatchRequest request(String userId) {
		return new CurrentMatchRequest(userId);
	}

	public String getUserId() {
		return userId;
	}
}
