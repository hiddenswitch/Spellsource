package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class CreateAccountRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String emailAddress;
	private String password;
	private boolean bot;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}

	public CreateAccountRequest withName(final String name) {
		this.name = name;
		return this;
	}

	public CreateAccountRequest withEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
		return this;
	}

	public CreateAccountRequest withPassword(final String password) {
		this.password = password;
		return this;
	}

	public CreateAccountRequest withBot(final boolean bot) {
		this.bot = bot;
		return this;
	}
}
