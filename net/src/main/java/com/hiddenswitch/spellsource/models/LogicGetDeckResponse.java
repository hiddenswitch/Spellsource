package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public class LogicGetDeckResponse implements Serializable{
	private GetCollectionResponse collectionResponse;

	public GetCollectionResponse getCollection() {
		return collectionResponse;
	}

	public void setCollection(GetCollectionResponse collection) {
		this.collectionResponse = collection;
	}
}
