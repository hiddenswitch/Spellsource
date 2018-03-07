package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 6/11/17.
 */
public final class GetDraftRequest implements Serializable {
	public String userId;

	public GetDraftRequest() {
	}

	public GetDraftRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}
}
