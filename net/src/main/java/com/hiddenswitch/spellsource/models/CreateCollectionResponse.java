package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCollectionResponse implements Serializable {
	private String collectionId;
	private List<String> createdInventoryIds;

	public List<String> getCreatedInventoryIds() {
		return createdInventoryIds;
	}

	public void setCreatedInventoryIds(List<String> createdInventoryIds) {
		this.createdInventoryIds = createdInventoryIds;
	}

	public CreateCollectionResponse withCreatedInventoryIds(final List<String> createdInventoryIds) {
		this.createdInventoryIds = createdInventoryIds;
		return this;
	}

	protected CreateCollectionResponse() {
	}

	public static CreateCollectionResponse user(String userId, List<String> newInventoryIds) {
		return new CreateCollectionResponse()
				.withCollectionId(userId)
				.withCreatedInventoryIds(newInventoryIds);
	}

	public static CreateCollectionResponse deck(String deckId) {
		return new CreateCollectionResponse()
				.withCollectionId(deckId);
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public CreateCollectionResponse withCollectionId(final String collectionId) {
		this.collectionId = collectionId;
		return this;
	}

	public static CreateCollectionResponse alliance(String allianceId) {
		return new CreateCollectionResponse()
				.withCollectionId(allianceId);
	}
}
