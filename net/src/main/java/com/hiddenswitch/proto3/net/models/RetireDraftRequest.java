package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class RetireDraftRequest implements Serializable {
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public RetireDraftRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
