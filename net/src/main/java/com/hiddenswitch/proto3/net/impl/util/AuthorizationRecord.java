package com.hiddenswitch.proto3.net.impl.util;

import java.io.Serializable;
import java.util.List;

/**
 * An AuthorizationRecord contains the sensitive information the server needs to authenticate clients.
 *
 * In this protocol, user passwords are hashed with Scrypt. The hash is stored on the server. Later, when the user
 * logs in with an email and password, the password is used with the Scrypt hash comparison function. If the password
 * matches the hash, a LoginToken is issued to the client.
 *
 * The LoginToken contains a public (user ID) and private (randomly generated string) portion. The server stores an
 * Scrypt hash of the private portion of the LoginToken. When the user authenticates using a token, the accounts
 * security system finds the appropriate user record with the public (user ID) field of the token. Then it compares the
 * secret to the stored hash of the secret. If it matches, the client is authorized.
 *
 * No secrets are ever leaked by this object. The stored data cannot be used by the public API to authenticate the user,
 * since the original secrets pre-hash (either user password or randomly generated token) were never stored.
 */
public class AuthorizationRecord implements Serializable {
	private List<HashedLoginSecret> tokens;
	private String scrypt;

	/**
	 * Gets the SCrypt-generated password hash that corresponds to this user's password.
	 * @return
	 */
	public String getScrypt() {
		return scrypt;
	}

	public void setScrypt(String scrypt) {
		this.scrypt = scrypt;
	}

	/**
	 * Gets a list of hashed tokens that authenticate HTTP API calls with X-Auth-Header strings.
	 * @return
	 */
	public List<HashedLoginSecret> getTokens() {
		return tokens;
	}

	public void setTokens(List<HashedLoginSecret> tokens) {
		this.tokens = tokens;
	}
}
