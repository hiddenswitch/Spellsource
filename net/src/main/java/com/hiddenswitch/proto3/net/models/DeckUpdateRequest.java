package com.hiddenswitch.proto3.net.models;


import com.hiddenswitch.proto3.net.client.models.DecksUpdateCommand;

import java.io.Serializable;

public class DeckUpdateRequest implements Serializable {
	private String userId;
	private String deckId;
	private DecksUpdateCommand updateCommand;

	public DeckUpdateRequest(String userId, String deckId, DecksUpdateCommand updateCommand) {
		this.userId = userId;
		this.deckId = deckId;
		this.updateCommand = updateCommand;
	}

	public DecksUpdateCommand getUpdateCommand() {
		return updateCommand;
	}

	public String getDeckId() {
		return deckId;
	}

	public String getUserId() {
		return userId;
	}
}
