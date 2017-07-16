package com.hiddenswitch.spellsource.impl.util;

import java.io.Serializable;

/**
 * A Profile object contains non-gameplay/cosmetic information about a user.
 */
public class Profile implements Serializable {
	private String emailAddress;
	private String name;

	/**
	 * The user's email address. Should not be shown to the public.
	 *
	 * @return Email address.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * Gets the user's display name (typically their username.
	 *
	 * @return A username.
	 */
	public String getDisplayName() {
		return name;
	}

	public void setDisplayName(String name) {
		this.name = name;
	}
}

