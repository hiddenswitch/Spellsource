package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class LoginRequest implements Serializable {
	private String email;
	private String password;

	public LoginRequest() {
	}

	public LoginRequest withEmail(String username) {
		this.email = username;
		return this;
	}

	public LoginRequest withPassword(String password) {
		this.password = password;
		return this;
	}

	public LoginRequest withToken(String token) {
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
