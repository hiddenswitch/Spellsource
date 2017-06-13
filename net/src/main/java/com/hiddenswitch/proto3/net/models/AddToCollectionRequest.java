package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class AddToCollectionRequest implements Serializable {
	private String collectionId;
	private List<String> inventoryIds;
	private List<String> cardIds;
	private String userId;
	private int copies = 1;

	public String getCollectionId() {
		return collectionId;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public AddToCollectionRequest() {
	}

	public AddToCollectionRequest(String collectionId, List<String> inventoryIds) {
		this.collectionId = collectionId;
		this.inventoryIds = inventoryIds;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}

	public AddToCollectionRequest withCollectionId(final String collectionId) {
		this.collectionId = collectionId;
		return this;
	}

	public AddToCollectionRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}

	public AddToCollectionRequest withCardIds(final List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public AddToCollectionRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public AddToCollectionRequest withCopies(int copies) {
		this.copies = copies;
		return this;
	}

	public int getCopies() {
		return copies;
	}

	public void setCopies(int copies) {
		this.copies = copies;
	}
}
