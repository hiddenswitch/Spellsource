package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

public class DeckCreateResponse implements Serializable {
	private final String deckId;
	private final List<String> inventoryIds;


	public DeckCreateResponse(String deckId, List<String> inventoryIds) {
		this.deckId = deckId;
		this.inventoryIds = inventoryIds;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public String getDeckId() {
		return deckId;
	}
}
