package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCollectionRequest implements Serializable {
	private CollectionTypes type;
	private String userId;
	private List<String> cardIds;
	private QueryCardsRequest queryCardsRequest;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public OpenCardPackRequest getOpenCardPackRequest() {
		return openCardPackRequest;
	}

	public void setOpenCardPackRequest(OpenCardPackRequest openCardPackRequest) {
		this.openCardPackRequest = openCardPackRequest;
	}

	private OpenCardPackRequest openCardPackRequest;

	public QueryCardsRequest getQueryCardsRequest() {
		return queryCardsRequest;
	}

	public void setQueryCardsRequest(QueryCardsRequest queryCardsRequest) {
		this.queryCardsRequest = queryCardsRequest;
	}

	public CreateCollectionRequest withType(CollectionTypes type) {
		this.type = type;
		return this;
	}

	public CreateCollectionRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public CollectionTypes getType() {
		return type;
	}

	public void setType(CollectionTypes type) {
		this.type = type;
	}

	public CreateCollectionRequest withCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	public CreateCollectionRequest withCardsQuery(QueryCardsRequest queryCardsRequest) {
		this.queryCardsRequest = queryCardsRequest;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public CreateCollectionRequest withOpenCardPack(OpenCardPackRequest openCardPackRequest) {
		this.openCardPackRequest = openCardPackRequest;
		return this;
	}
}
