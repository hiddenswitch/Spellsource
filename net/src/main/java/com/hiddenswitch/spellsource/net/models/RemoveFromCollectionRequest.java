package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;
import java.util.List;

public class RemoveFromCollectionRequest implements Serializable {
	private List<String> inventoryIds;
	private List<String> cardIds;
	private String collectionId;

	private RemoveFromCollectionRequest() {
	}

	private RemoveFromCollectionRequest(String collectionId, List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		this.collectionId = collectionId;
	}

	public static RemoveFromCollectionRequest byInventoryIds(String collectionId, List<String> inventoryIds) {
		return new RemoveFromCollectionRequest(collectionId, inventoryIds);
	}

	public static RemoveFromCollectionRequest byCardIds(String collectionId, List<String> cardIds) {
		RemoveFromCollectionRequest request = new RemoveFromCollectionRequest();
		request.setCollectionId(collectionId);
		request.setCardIds(cardIds);
		return request;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}
}
