package com.hiddenswitch.proto3.net.common;

import net.demilich.metastone.game.decks.Deck;

import java.io.Serializable;

/**
 * Created by bberman on 1/23/17.
 */
public class MatchmakingQueuePut implements Serializable {
	protected Deck deck;
	protected boolean allowBots;
	private String deckId;
	private String clientType;

	public MatchmakingQueuePut() {
	}

	public MatchmakingQueuePut(MatchmakingRequest matchmakingRequest) {
		this.deck = matchmakingRequest.getDeck();
		this.allowBots = matchmakingRequest.isAllowBots();
		this.deckId = matchmakingRequest.getDeckId();
		this.clientType = matchmakingRequest.getClientType();
	}

	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public boolean isAllowBots() {
		return allowBots;
	}

	public void setAllowBots(boolean allowBots) {
		this.allowBots = allowBots;
	}

	public MatchmakingQueuePut withDeck(final Deck deck) {
		this.deck = deck;
		return this;
	}

	public MatchmakingQueuePut withAllowBots(final boolean allowBots) {
		this.allowBots = allowBots;
		return this;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public MatchmakingQueuePut withDeckId(final String deckId) {
		this.deckId = deckId;
		return this;
	}

	public MatchmakingQueuePut withClientType(final String clientType) {
		this.clientType = clientType;
		return this;
	}
}
