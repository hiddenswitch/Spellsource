package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class BorrowFromCollectionResponse implements Serializable {
	private final long recordsBorrowed;

	public BorrowFromCollectionResponse(long recordsBorrowed) {
		this.recordsBorrowed = recordsBorrowed;
	}
}
