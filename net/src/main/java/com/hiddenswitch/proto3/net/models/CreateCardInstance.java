package com.hiddenswitch.proto3.net.models;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCardInstance {
	private String ownerUserId;
	private String cardId;

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
}
