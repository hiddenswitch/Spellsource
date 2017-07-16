package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 2/16/17.
 */
public class TrashCollectionResponse implements Serializable {
	private final boolean trashed;
	private final long cardsRemoved;

	public TrashCollectionResponse(boolean trashed, long cardsRemoved) {
		this.trashed = trashed;
		this.cardsRemoved = cardsRemoved;
	}

	public boolean isTrashed() {
		return trashed;
	}

	public long getCardsRemoved() {
		return cardsRemoved;
	}
}
