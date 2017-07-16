package com.hiddenswitch.spellsource.impl.util;

import com.hiddenswitch.spellsource.models.LoginResponse;

import java.io.Serializable;

/**
 * Created by bberman on 1/26/17.
 */
public class AuthenticationException extends Throwable implements Serializable {
	private final LoginResponse response;

	public AuthenticationException(LoginResponse response) {
		this.response = response;
	}

	public LoginResponse getResponse() {
		return response;
	}
}
