package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 2/16/17.
 */
public class RemoveFromCollectionRequest implements Serializable {
	private List<String> inventoryIds;
	private String collectionId;

	public RemoveFromCollectionRequest(String collectionId, List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		this.collectionId = collectionId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
}
