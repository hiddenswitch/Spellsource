package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class GetCardRequest implements Serializable {
	String cardId;

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
