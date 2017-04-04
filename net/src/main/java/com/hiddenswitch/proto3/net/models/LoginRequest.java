package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class LoginRequest implements Serializable {
	private String email;
	private String password;

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
