package com.hiddenswitch.proto3.net.impl.util;

import com.lambdaworks.crypto.SCryptUtil;

import java.io.Serializable;
import java.util.Date;

public class HashedLoginSecret implements Serializable {
	private String hashedLoginToken;
	private Date expiresAt;

	public HashedLoginSecret() {
	}

	public HashedLoginSecret(LoginToken token) {
		setHashedLoginToken(SCryptUtil.scrypt(token.getSecret(), 256, 4, 1));
		setExpiresAt(token.expiresAt);
	}

	public String getHashedLoginToken() {
		return hashedLoginToken;
	}

	public void setHashedLoginToken(String hashedLoginToken) {
		this.hashedLoginToken = hashedLoginToken;
	}

	public Date getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
	}
}
