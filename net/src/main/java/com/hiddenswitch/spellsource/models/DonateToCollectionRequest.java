package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 3/4/17.
 */
public class DonateToCollectionRequest implements Serializable {
	private final String allianceId;
	private final List<String> inventoryIds;

	public DonateToCollectionRequest(String allianceId, List<String> inventoryIds) {

		this.allianceId = allianceId;
		this.inventoryIds = inventoryIds;
	}

	public String getAllianceId() {
		return allianceId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}
}
