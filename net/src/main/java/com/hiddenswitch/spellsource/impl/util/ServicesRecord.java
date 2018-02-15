package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * An ServicesRecord contains the sensitive information the server needs to authenticate clients.
 * <p>
 * In this protocol, user passwords are hashed with sha256. The hash is stored on the server. Later, when the user logs
 * in with an email and password, the password is used with the Scrypt hash comparison function. If the password matches
 * the hash, a LoginToken is issued to the client.
 * <p>
 * The {@link LoginToken} contains a public (user ID) and private (randomly generated string) portion. The server stores
 * an Scrypt hash of the private portion of the LoginToken. When the user authenticates using a token, the accounts
 * security system finds the appropriate user record with the public (user ID) field of the token. Then it compares the
 * secret to the stored hash of the secret. If it matches, the client is authorized.
 * <p>
 * No secrets are ever leaked by this object. The stored data cannot be used by the public API to authenticate the user,
 * since the original secrets pre-hash (either user password or randomly generated token) were never stored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicesRecord implements Serializable {
	private PasswordRecord password;
	private ResumeRecord resume;

	public PasswordRecord getPassword() {
		return password;
	}

	public void setPassword(PasswordRecord password) {
		this.password = password;
	}

	public ResumeRecord getResume() {
		return resume;
	}

	public void setResume(ResumeRecord resume) {
		this.resume = resume;
	}
}
