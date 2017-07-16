package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/1/17.
 */
public class CurrentMatchRequest implements Serializable {
	private String userId;

	public CurrentMatchRequest(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
