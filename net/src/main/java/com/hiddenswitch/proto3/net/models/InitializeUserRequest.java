package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/30/17.
 */
public class InitializeUserRequest implements Serializable {
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public InitializeUserRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
