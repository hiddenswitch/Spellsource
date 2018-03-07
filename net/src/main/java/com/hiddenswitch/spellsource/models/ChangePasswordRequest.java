package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;

public final class ChangePasswordRequest implements Serializable {
	private String password;
	private UserId userId;

	private ChangePasswordRequest() {
	}

	private ChangePasswordRequest(UserId userId, String password) {
		this.password = password;
		this.userId = userId;
	}

	public static ChangePasswordRequest request(UserId userId, String password) {
		return new ChangePasswordRequest(userId, password);
	}

	public String getPassword() {
		return password;
	}

	public UserId getUserId() {
		return userId;
	}
}
