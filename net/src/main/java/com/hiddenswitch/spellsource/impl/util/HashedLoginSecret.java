package com.hiddenswitch.spellsource.impl.util;

import com.lambdaworks.crypto.SCryptUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents the hashed secret portion of the token string.
 */
public class HashedLoginSecret implements Serializable {
	private String hashedLoginToken;
	private Date expiresAt;

	public HashedLoginSecret() {
	}

	/**
	 * Hash a LoginToken to produce a hashed login secret.
	 * @param token A LoginToken object.
	 */
	public HashedLoginSecret(LoginToken token) {
		setHashedLoginToken(SCryptUtil.scrypt(token.getSecret(), 256, 4, 1));
		setExpiresAt(token.getExpiresAt());
	}

	/**
	 * Gets the hashed login token for comparisons using Scrypt.
	 * @return The hash of the secret part of the login token corresponding to this user.
	 */
	public String getHashedLoginToken() {
		return hashedLoginToken;
	}

	public void setHashedLoginToken(String hashedLoginToken) {
		this.hashedLoginToken = hashedLoginToken;
	}

	/**
	 * Get the expiration of those token.
	 * @return The expiration date.
	 */
	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
