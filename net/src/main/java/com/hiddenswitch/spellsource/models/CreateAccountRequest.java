package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public class CreateAccountRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String emailAddress;
	private String password;
	private boolean bot;

	/**
	 * The username. Visible to opponents. Not used for logging in.
	 * @return The username.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The user's email address. Not visible to the public. Used for logging in.
	 * @return The email.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * A plaintext password. Must be at least 6 characters long.
	 * @return The plaintext password.
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Set to true if the account should be a special bot account, managed by the server to play bot games.
	 * @return True if the newly created account should be owned by a bot.
	 */
	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}

	/**
	 * The username. Visible to opponents. Not used for logging in.
	 * @param name The name to set.
	 * @return
	 */
	public CreateAccountRequest withName(final String name) {
		this.name = name;
		return this;
	}

	/**
	 * The user's email address. Not visible to the public. Used for logging in.
	 * @param emailAddress The email address.
	 * @return
	 */
	public CreateAccountRequest withEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
		return this;
	}

	/**
	 * A plaintext password. Must be at least 6 characters long.
	 * @param password The password to set.
	 * @return
	 */
	public CreateAccountRequest withPassword(final String password) {
		this.password = password;
		return this;
	}

	/**
	 * Set to true if the account should be a special bot account, managed by the server to play bot games.
	 * @param bot Whether or not this is a bot
	 * @return
	 */
	public CreateAccountRequest withBot(final boolean bot) {
		this.bot = bot;
		return this;
	}
}
