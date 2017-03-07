package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class BorrowFromCollectionRequest implements Serializable {
	private String collectionId;
	private List<String> collectionIds;
	private String userId;
	private List<String> inventoryIds;

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public BorrowFromCollectionRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public BorrowFromCollectionRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}
}


