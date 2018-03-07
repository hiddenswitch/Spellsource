package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/30/17.
 */
public final class InitializeUserRequest implements Serializable {
	private String userId;

	private InitializeUserRequest() {
	}

	private InitializeUserRequest(String userId) {
		setUserId(userId);
	}

	public static InitializeUserRequest create(String userId) {
		return new InitializeUserRequest(userId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public InitializeUserRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
