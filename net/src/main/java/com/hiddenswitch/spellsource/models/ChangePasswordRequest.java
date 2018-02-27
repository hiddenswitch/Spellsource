package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;

public class ChangePasswordRequest implements Serializable {
	private final String password;
	private final UserId userId;

	public ChangePasswordRequest(UserId userId, String password) {
		this.password = password;
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public UserId getUserId() {
		return userId;
	}
}
