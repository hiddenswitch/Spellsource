package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class MatchCancelRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userId;

	public MatchCancelRequest() {
	}

	private MatchCancelRequest(String userId) {
		this.userId = userId;
	}

	public static MatchCancelRequest create(String userId) {
		return new MatchCancelRequest(userId);
	}

	public String getUserId() {
		return userId;
	}
}
