package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/30/17.
 */
public class InitializeUserResponse implements Serializable {
	private CreateCollectionResponse createCollectionResponse;

	public CreateCollectionResponse getCreateCollectionResponse() {
		return createCollectionResponse;
	}

	public void setCreateCollectionResponse(CreateCollectionResponse createCollectionResponse) {
		this.createCollectionResponse = createCollectionResponse;
	}

	public InitializeUserResponse withCreateCollectionResponse(final CreateCollectionResponse createCollectionResponse) {
		this.createCollectionResponse = createCollectionResponse;
		return this;
	}
}
