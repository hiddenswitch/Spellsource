package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by bberman on 1/22/17.
 */
public class GetCollectionResponse implements Serializable {
	private Set<InventoryRecord> inventoryRecords;

	public Set<InventoryRecord> getInventoryRecords() {
		return inventoryRecords;
	}

	public void setInventoryRecords(Set<InventoryRecord> inventoryRecords) {
		this.inventoryRecords = inventoryRecords;
	}

	public GetCollectionResponse withInventoryRecords(Set<InventoryRecord> inventoryRecords) {
		this.inventoryRecords = inventoryRecords;
		return this;
	}
}
