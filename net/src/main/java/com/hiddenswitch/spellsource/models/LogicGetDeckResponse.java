package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class LogicGetDeckResponse implements Serializable {
	private GetCollectionResponse collectionResponse;

	public LogicGetDeckResponse() {
	}

	public GetCollectionResponse getCollection() {
		return collectionResponse;
	}

	public void setCollection(GetCollectionResponse collection) {
		this.collectionResponse = collection;
	}
}
