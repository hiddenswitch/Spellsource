package com.hiddenswitch.proto3.net.amazon;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class LoginToken implements Serializable {
	public Date expiresAt;
	public String token;

	public static LoginToken createSecure(String userId) {
		SecureRandom random = new SecureRandom();
		return new LoginToken(Date.from(Instant.now().plus(60L * 60L * 24L * 365L, ChronoUnit.SECONDS)), userId, new BigInteger(130, random).toString(32));
	}

	protected LoginToken() {
	}

	@JsonIgnore
	public String getSecret() {
		return this.token.split(":")[1];
	}

	@JsonIgnore
	public String getAccessKey() {
		return this.token.split(":")[0];
	}

	protected LoginToken(Date expiresAt, String accessKey, String secret) {
		this.expiresAt = expiresAt;
		this.token = accessKey + ":" + secret;
	}
}
