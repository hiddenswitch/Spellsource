package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutRequest;

import java.io.Serializable;

/**
 * Created by bberman on 1/23/17.
 */
public class MatchmakingRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	protected MatchmakingDeck deck;
	protected boolean allowBots;
	protected String userId;
	private String deckId;
	private String clientType;

	public MatchmakingRequest() {
	}

	public MatchmakingRequest(MatchmakingRequest matchmakingRequest) {
		this.deck = matchmakingRequest.getDeck();
		this.allowBots = matchmakingRequest.isAllowBots();
		this.deckId = matchmakingRequest.getDeckId();
		this.clientType = matchmakingRequest.getClientType();
	}

	public MatchmakingRequest(MatchmakingQueuePutRequest other, String userId) {
		this.deck = other.getDeck();
		this.userId = userId;
	}

	public MatchmakingDeck getDeck() {
		return deck;
	}

	public void setDeck(MatchmakingDeck deck) {
		this.deck = deck;
	}

	public boolean isAllowBots() {
		return allowBots;
	}

	public void setAllowBots(boolean allowBots) {
		this.allowBots = allowBots;
	}

	public MatchmakingRequest withDeck(final MatchmakingDeck deck) {
		this.deck = deck;
		return this;
	}

	public MatchmakingRequest withAllowBots(final boolean allowBots) {
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

	public MatchmakingRequest withDeckId(final String deckId) {
		this.deckId = deckId;
		return this;
	}

	public MatchmakingRequest withClientType(final String clientType) {
		this.clientType = clientType;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public MatchmakingRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}
}
