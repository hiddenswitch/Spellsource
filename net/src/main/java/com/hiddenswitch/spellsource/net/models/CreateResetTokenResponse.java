package com.hiddenswitch.spellsource.net.models;

public class CreateResetTokenResponse {
	private String id;
	private String token;

	public String getId() {
		return id;
	}

	public CreateResetTokenResponse setId(String id) {
		this.id = id;
		return this;
	}

	public String getToken() {
		return token;
	}

	public CreateResetTokenResponse setToken(String token) {
		this.token = token;
		return this;
	}
}
