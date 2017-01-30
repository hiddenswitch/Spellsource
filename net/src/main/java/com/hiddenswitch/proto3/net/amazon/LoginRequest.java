package com.hiddenswitch.proto3.net.amazon;

import java.io.Serializable;

public class LoginRequest implements Serializable {
	private String userId;
	private String password;
	private String token;

	public LoginRequest withUserId(String username) {
		this.userId = username;
		return this;
	}

	public LoginRequest withPassword(String password) {
		this.password = password;
		return this;
	}

	public LoginRequest withToken(String token) {
		this.token = token;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
