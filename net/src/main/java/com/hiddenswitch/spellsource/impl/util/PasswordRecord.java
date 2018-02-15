package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordRecord implements Serializable {
	private String bcrypt;
	private String scrypt;

	public String getBcrypt() {
		return bcrypt;
	}

	public void setBcrypt(String bcrypt) {
		this.bcrypt = bcrypt;
	}

	public String getScrypt() {
		return scrypt;
	}

	public void setScrypt(String scrypt) {
		this.scrypt = scrypt;
	}
}
