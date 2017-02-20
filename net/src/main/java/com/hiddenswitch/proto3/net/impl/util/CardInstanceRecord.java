package com.hiddenswitch.proto3.net.impl.util;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Created by bberman on 1/19/17.
 */
public class CardInstanceRecord {
	private final String ownerUserId;
	private final String cardId;
	private String id;

	public CardInstanceRecord(String ownerUserId, String cardId) {
		this.id = RandomStringUtils.randomAlphanumeric(12).toLowerCase();
		this.ownerUserId = ownerUserId;
		this.cardId = cardId;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public String getCardId() {
		return cardId;
	}

	public String getId() {
		return id;
	}
}
