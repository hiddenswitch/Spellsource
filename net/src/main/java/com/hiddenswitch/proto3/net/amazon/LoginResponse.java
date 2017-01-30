package com.hiddenswitch.proto3.net.amazon;

import java.io.Serializable;

public class LoginResponse implements Serializable {
	private LoginToken token;
	private UserRecord record;
	private boolean badUsername;
	private boolean badPassword;
	private boolean badToken;

	public LoginResponse(LoginToken token, UserRecord record) {
		this.token = token;
		this.record = record;
		this.badPassword = false;
		this.badUsername = false;
	}

	public LoginResponse(boolean badUsername, boolean badPassword) {
		this.token = null;
		this.record = null;
		this.badPassword = badPassword;
		this.badUsername = badUsername;
	}

	public LoginToken getToken() {
		return token;
	}

	public UserRecord getRecord() {
		return record;
	}

	public boolean isBadUsername() {
		return badUsername;
	}

	public boolean isBadPassword() {
		return badPassword;
	}

	public boolean isBadToken() {
		return badToken;
	}
}
