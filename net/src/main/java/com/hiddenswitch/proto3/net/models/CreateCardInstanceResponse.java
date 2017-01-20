package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class CreateCardInstanceResponse implements Serializable {
	private final String cardInstanceId;

	public CreateCardInstanceResponse(String cardInstanceId) {

		this.cardInstanceId = cardInstanceId;
	}

	public String getCardInstanceId() {
		return cardInstanceId;
	}
}
