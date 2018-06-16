package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.client.models.MatchmakingDeck;
import com.hiddenswitch.spellsource.client.models.MatchmakingQueuePutRequest;

import java.io.Serializable;

/**
 * Created by bberman on 1/23/17.
 */
public class MatchmakingRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	protected MatchmakingDeck deck;
	protected boolean allowBots;
	protected String userId;
	private boolean botMatch;
	private String deckId;
	private String clientType;
	private String botDeckId;
	private int timeout = 45000;
	private String queueId;

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
		this.deckId = other.getDeckId();
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

	public boolean isBotMatch() {
		return botMatch;
	}

	public void setBotMatch(boolean botMatch) {
		this.botMatch = botMatch;
	}

	public MatchmakingRequest withBotMatch(final boolean botMatch) {
		this.botMatch = botMatch;
		return this;
	}

	public String getBotDeckId() {
		return botDeckId;
	}

	public void setBotDeckId(String botDeckId) {
		this.botDeckId = botDeckId;
	}

	public MatchmakingRequest withBotDeckId(String botDeckId) {
		this.botDeckId = botDeckId;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public MatchmakingRequest withTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public String getQueueId() {
		return queueId;
	}

	public MatchmakingRequest setQueueId(String queueId) {
		this.queueId = queueId;
		return this;
	}
}
