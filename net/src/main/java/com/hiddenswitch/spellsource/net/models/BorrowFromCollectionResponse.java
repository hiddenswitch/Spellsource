package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;

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
