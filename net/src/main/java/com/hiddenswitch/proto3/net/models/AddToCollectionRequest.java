package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class AddToCollectionRequest implements Serializable {
	private final String collectionId;
	private final List<String> inventoryIds;

	public String getCollectionId() {
		return collectionId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public AddToCollectionRequest(String collectionId, List<String> inventoryIds) {
		this.collectionId = collectionId;
		this.inventoryIds = inventoryIds;
	}
}
