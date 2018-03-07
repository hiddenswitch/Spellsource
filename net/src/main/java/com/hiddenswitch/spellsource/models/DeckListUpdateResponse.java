package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public final class DeckListUpdateResponse implements Serializable {
	private long updatedCount;

	private DeckListUpdateResponse() {
	}

	private DeckListUpdateResponse(long updatedCount) {
		this.updatedCount = updatedCount;
	}

	public static DeckListUpdateResponse create(long updatedCount) {
		return new DeckListUpdateResponse(updatedCount);
	}

	public long getUpdatedCount() {
		return updatedCount;
	}
}
