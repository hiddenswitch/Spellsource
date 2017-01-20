package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCollectionRequest implements Serializable {
	private CollectionTypes type;
	private String userId;

	public CreateCollectionRequest withType(CollectionTypes type) {
		this.type = type;
		return this;
	}

	public CreateCollectionRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public CollectionTypes getType() {
		return type;
	}

	public void setType(CollectionTypes type) {
		this.type = type;
	}
}
