package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;
import java.util.List;

public final class DonateToCollectionRequest implements Serializable {
	private String allianceId;
	private List<String> inventoryIds;

	private DonateToCollectionRequest() {
	}

	private DonateToCollectionRequest(String allianceId, List<String> inventoryIds) {

		this.allianceId = allianceId;
		this.inventoryIds = inventoryIds;
	}

	public static DonateToCollectionRequest create(String allianceId, List<String> inventoryIds) {
		return new DonateToCollectionRequest(allianceId, inventoryIds);
	}

	public String getAllianceId() {
		return allianceId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}
}
