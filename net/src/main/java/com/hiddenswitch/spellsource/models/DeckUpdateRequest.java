package com.hiddenswitch.spellsource.models;


import com.hiddenswitch.spellsource.client.models.DecksUpdateCommand;

import java.io.Serializable;

public final class DeckUpdateRequest implements Serializable {
	private String userId;
	private String deckId;
	private DecksUpdateCommand updateCommand;

	private DeckUpdateRequest() {
	}

	private DeckUpdateRequest(String userId, String deckId, DecksUpdateCommand updateCommand) {
		this.userId = userId;
		this.deckId = deckId;
		this.updateCommand = updateCommand;
	}

	public static DeckUpdateRequest create(String userId, String deckId, DecksUpdateCommand updateCommand) {
		return new DeckUpdateRequest(userId, deckId, updateCommand);
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
