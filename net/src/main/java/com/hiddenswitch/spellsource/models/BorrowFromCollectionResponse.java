package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public final class BorrowFromCollectionResponse implements Serializable {
	private long recordsBorrowed;

	private BorrowFromCollectionResponse() {
	}

	private BorrowFromCollectionResponse(long recordsBorrowed) {
		this.recordsBorrowed = recordsBorrowed;
	}

	public static BorrowFromCollectionResponse response(long recordsBorrowed) {
		return new BorrowFromCollectionResponse(recordsBorrowed);
	}
}
