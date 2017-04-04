package com.hiddenswitch.proto3.net.impl.util;

import java.io.Serializable;

public class Profile implements Serializable {
	private String emailAddress;
	private String name;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getDisplayName() {
		return name;
	}

	public void setDisplayName(String name) {
		this.name = name;
	}
}

