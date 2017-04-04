package com.hiddenswitch.proto3.net.impl.util;

import java.io.Serializable;
import java.util.List;

public class AuthorizationRecord implements Serializable {
	private List<HashedLoginSecret> tokens;
	private String scrypt;

	public String getScrypt() {
		return scrypt;
	}

	public void setScrypt(String scrypt) {
		this.scrypt = scrypt;
	}

	public List<HashedLoginSecret> getTokens() {
		return tokens;
	}

	public void setTokens(List<HashedLoginSecret> tokens) {
		this.tokens = tokens;
	}
}
