package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;
import java.util.List;

public class SetCollectionRequest implements Serializable {
	private final String collectionId;
	private final List<String> inventoryIds;

	public String getCollectionId() {
		return collectionId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public SetCollectionRequest(String collectionId, List<String> inventoryIds) {
		this.collectionId = collectionId;
		this.inventoryIds = inventoryIds;
	}
}
