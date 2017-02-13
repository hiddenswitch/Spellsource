package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class BorrowFromCollectionRequest implements Serializable {
	private String collectionId;
	private List<String> collectionIds;

	public BorrowFromCollectionRequest withCollectionId(String collectionId) {
		this.collectionId = collectionId;
		return this;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public List<String> getCollectionIds() {
		return collectionIds;
	}

	public void setCollectionIds(List<String> collectionIds) {
		this.collectionIds = collectionIds;
	}

	public BorrowFromCollectionRequest withCollectionIds(final List<String> collectionIds) {
		this.collectionIds = collectionIds;
		return this;
	}
}


