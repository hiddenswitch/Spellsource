package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.util.LoginToken;

import java.io.Serializable;

/**
 * The results of creating an account.
 */
public class CreateAccountResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private LoginToken loginToken;
	private String userId;
	private boolean invalidName;
	private boolean invalidEmailAddress;
	private boolean invalidPassword;

	/**
	 * The login token object that the client should use for future calls to APIs.
	 * <p>
	 * If this is null, the account creation failed.
	 *
	 * @return A LoginToken object.
	 */
	public LoginToken getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(LoginToken loginToken) {
		this.loginToken = loginToken;
	}

	/**
	 * The newly created user ID for the specified request.
	 *
	 * @return The User ID.
	 */
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * When account creation failed, this field indicates that the reason was an invalid name.
	 *
	 * @return True if the name was invalid.
	 */
	public boolean isInvalidName() {
		return invalidName;
	}

	public void setInvalidName(boolean invalidName) {
		this.invalidName = invalidName;
	}

	/**
	 * When account creation failed, this field indicates that the reason was an invalid email.
	 *
	 * @return True if the email was invalid.
	 */
	public boolean isInvalidEmailAddress() {
		return invalidEmailAddress;
	}

	public void setInvalidEmailAddress(boolean invalidEmailAddress) {
		this.invalidEmailAddress = invalidEmailAddress;
	}

	/**
	 * When account creation failed, this field indicates that the reason was an invalid password.
	 *
	 * @return True if the password was invalid.
	 */
	public boolean isInvalidPassword() {
		return invalidPassword;
	}

	public void setInvalidPassword(boolean invalidPassword) {
		this.invalidPassword = invalidPassword;
	}

	/**
	 * Did the account creation succeed?
	 *
	 * @return True if the creation succeeded and there is a valid user ID and login token.
	 */
	public boolean succeeded() {
		return getLoginToken() != null
				&& getUserId() != null;
	}
}
