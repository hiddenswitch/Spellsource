package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.util.LoginToken;
import com.hiddenswitch.spellsource.impl.util.UserRecord;

import java.io.Serializable;

/**
 * Contains the results of a login.
 */
public class LoginResponse implements Serializable {
	private LoginToken token;
	private UserRecord record;
	private boolean badEmail;
	private boolean badPassword;

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

	/**
	 * The token the client should use in the X-Auth-Token header field for future HTTP API calls.
	 * @return The token object.
	 */
	public LoginToken getToken() {
		return token;
	}

	/**
	 * A complete view of the user record, including all internal information.
	 * @return The UserRecord object.
	 */
	public UserRecord getRecord() {
		return record;
	}

	/**
	 * A bad email address was provided and login failed.
	 * @return True if the email address is bad.
	 */
	public boolean isBadEmail() {
		return badEmail;
	}

	/**
	 * A bad password was provided for the specified email and login failed.
	 * @return True if the password for the specified email was bad.
	 */
	public boolean isBadPassword() {
		return badPassword;
	}

	public boolean succeeded() {
		return getToken() != null;
	}
}
