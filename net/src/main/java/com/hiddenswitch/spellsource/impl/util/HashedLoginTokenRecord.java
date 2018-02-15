package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hiddenswitch.spellsource.Accounts;
import com.lambdaworks.crypto.SCryptUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents the hashed secret portion of the token string.
 */
public class HashedLoginTokenRecord implements Serializable {

	private String hashedToken;
	private Date when;

	public HashedLoginTokenRecord() {
	}

	/**
	 * Hash a LoginToken to produce a hashed login secret.
	 *
	 * @param token A LoginToken object.
	 */
	public HashedLoginTokenRecord(LoginToken token) {
		setHashedToken(Accounts.hash(token.getSecret()));
		setWhen(token.getExpiresAt());
	}

	/**
	 * Gets the hashed login token for comparisons using Scrypt.
	 *
	 * @return The hash of the secret part of the login token corresponding to this user.
	 */
	public String getHashedToken() {
		return hashedToken;
	}

	public void setHashedToken(String hashedToken) {
		this.hashedToken = hashedToken;
	}

	/**
	 * Get the expiration of those token.
	 *
	 * @return The expiration date.
	 */
	public Date getWhen() {
		return when;
	}


	public void setWhen(Date when) {
		this.when = when;
	}

	/**
	 * Checks if the token validates the given secret.
	 *
	 * @param secret A secret.
	 * @return {@code true} if the token matches.
	 */
	@JsonIgnore
	public boolean check(String secret) {
		boolean sha256Comparison = Accounts.hash(secret).equals(hashedToken);
		if (sha256Comparison) {
			return true;
		}

		boolean scryptComparison = false;
		try {
			scryptComparison = SCryptUtil.check(secret, hashedToken);
		} catch (IllegalArgumentException unknownTokenFormat) {
			return false;
		}
		return scryptComparison;
	}
}
