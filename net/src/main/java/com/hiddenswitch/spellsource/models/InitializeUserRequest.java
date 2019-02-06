package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Indicates a request to initialize the specified user.
 */
public final class InitializeUserRequest implements Serializable {
	private String userId;

	public InitializeUserRequest() {
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
