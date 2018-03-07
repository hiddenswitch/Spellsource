package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 3/4/17.
 */
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
