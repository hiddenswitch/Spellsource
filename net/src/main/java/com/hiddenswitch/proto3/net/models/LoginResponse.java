package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.util.LoginToken;
import com.hiddenswitch.proto3.net.impl.util.UserRecord;

import java.io.Serializable;

public class LoginResponse implements Serializable {
	private LoginToken token;
	private UserRecord record;
	private boolean badEmail;
	private boolean badPassword;
	private boolean badToken;

	public LoginResponse(LoginToken token, UserRecord record) {
		this.token = token;
		this.record = record;
		this.badPassword = false;
		this.badEmail = false;
	}

	public LoginResponse(boolean badEmail, boolean badPassword) {
		this.token = null;
		this.record = null;
		this.badPassword = badPassword;
		this.badEmail = badEmail;
	}

	public LoginToken getToken() {
		return token;
	}

	public UserRecord getRecord() {
		return record;
	}

	public boolean isBadEmail() {
		return badEmail;
	}

	public boolean isBadPassword() {
		return badPassword;
	}

	public boolean isBadToken() {
		return badToken;
	}
}
