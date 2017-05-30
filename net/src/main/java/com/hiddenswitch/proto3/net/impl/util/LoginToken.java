package com.hiddenswitch.proto3.net.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * A LoginToken contains the expiration date and token string to be passed to API calls by clients.
 */
public class LoginToken implements Serializable {
	private Date expiresAt;
	private String token;

	/**
	 * Create a secure token for the given user ID. Uses java.security.SecureRandom internally with a year-long
	 * expiration.
	 * @param userId The user ID to create the token for. This becomes the public part / access key of the token.
	 * @return A LoginToken object.
	 */
	public static LoginToken createSecure(String userId) {
		SecureRandom random = new SecureRandom();
		return new LoginToken(Date.from(Instant.now().plus(60L * 60L * 24L * 365L, ChronoUnit.SECONDS)), userId, new BigInteger(130, random).toString(32));
	}

	protected LoginToken() {
	}

	/**
	 * Get the secret portion of the token string.
	 * @return The token secret.
	 */
	@JsonIgnore
	public String getSecret() {
		return this.token.split(":")[1];
	}

	/**
	 * Get the public portion of the token string (typically the User ID).
	 * @return The token access key.
	 */
	@JsonIgnore
	public String getAccessKey() {
		return this.token.split(":")[0];
	}

	protected LoginToken(Date expiresAt, String accessKey, String secret) {
		this.expiresAt = expiresAt;
		this.token = accessKey + ":" + secret;
	}

	/**
	 * Gets the complete login token clients should use in the HTTP API's X-Auth-Token header.
	 * @return The token.
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Gets the token's expiration date.
	 * @return The token's expiration date.
	 */
	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
