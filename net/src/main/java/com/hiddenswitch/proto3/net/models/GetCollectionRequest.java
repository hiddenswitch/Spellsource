package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/22/17.
 */
public class GetCollectionRequest implements Serializable {
	private String userId;

	public GetCollectionRequest(String userId) {
		this.userId = userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserId() {
		return userId;
	}

	public GetCollectionRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}
}


