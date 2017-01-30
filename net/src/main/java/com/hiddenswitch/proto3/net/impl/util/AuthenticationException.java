package com.hiddenswitch.proto3.net.impl.util;

import com.hiddenswitch.proto3.net.amazon.LoginResponse;
import io.vertx.ext.auth.User;

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
