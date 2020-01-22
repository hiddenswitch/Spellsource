package com.hiddenswitch.spellsource.micro;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class RequestTraceDeck {
	private int playerId;
	private List<String> cardIds;

	public int getPlayerId() {
		return playerId;
	}

	public RequestTraceDeck setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public RequestTraceDeck setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	public RequestTraceDeck() {
	}
}
