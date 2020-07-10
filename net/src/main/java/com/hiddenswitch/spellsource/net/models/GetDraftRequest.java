package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;

public final class GetDraftRequest implements Serializable {
	public String userId;

	public GetDraftRequest() {
	}

	public GetDraftRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}
}
