package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

public class DeckDeleteResponse implements Serializable {
	private final TrashCollectionResponse response;

	public DeckDeleteResponse(TrashCollectionResponse response) {
		this.response = response;
	}

	public TrashCollectionResponse getResponse() {
		return response;
	}
}
