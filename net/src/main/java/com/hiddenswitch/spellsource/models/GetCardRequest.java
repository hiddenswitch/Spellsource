package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public final class GetCardRequest implements Serializable {
	private String cardId;

	public GetCardRequest() {
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public GetCardRequest withCardId(final String cardId) {
		this.cardId = cardId;
		return this;
	}
}
