package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class DeckCreateResponse implements Serializable {
	private final String deckId;
	private final GetCollectionResponse collection;


	public DeckCreateResponse(String deckId, GetCollectionResponse collection) {
		this.deckId = deckId;
		this.collection = collection;
	}

	public List<String> getInventoryIds() {
		return collection.getInventoryRecords().stream().map(InventoryRecord::getId).collect(Collectors.toList());
	}

	public String getDeckId() {
		return deckId;
	}

	public GetCollectionResponse getCollection() {
		return collection;
	}
}
