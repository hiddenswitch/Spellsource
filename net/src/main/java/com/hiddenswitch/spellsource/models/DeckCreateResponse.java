package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.util.InventoryRecord;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public final class DeckCreateResponse implements Serializable {
	private String deckId;
	private GetCollectionResponse collection;


	private DeckCreateResponse(String deckId, GetCollectionResponse collection) {
		this.deckId = deckId;
		this.collection = collection;
	}

	public static DeckCreateResponse create(String deckId, GetCollectionResponse collection) {
		return new DeckCreateResponse(deckId, collection);
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
