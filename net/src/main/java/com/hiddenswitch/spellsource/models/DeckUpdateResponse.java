package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class DeckUpdateResponse implements Serializable {
	private List<String> addedInventoryIds = Collections.emptyList();
	private List<String> removedInventoryIds = Collections.emptyList();

	private DeckUpdateResponse() {
	}

	public static DeckUpdateResponse changed(List<String> addedInventoryIds, List<String> removedInventoryIds) {
		DeckUpdateResponse response = new DeckUpdateResponse();
		response.addedInventoryIds = addedInventoryIds;
		response.removedInventoryIds = removedInventoryIds;
		return response;
	}

	public List<String> getAddedInventoryIds() {
		return Collections.unmodifiableList(addedInventoryIds);
	}

	public List<String> getRemovedInventoryIds() {
		return Collections.unmodifiableList(removedInventoryIds);
	}
}
