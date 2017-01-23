package com.hiddenswitch.proto3.net.common;

import net.demilich.metastone.game.decks.Bench;
import net.demilich.metastone.game.decks.Deck;

import java.io.Serializable;

public class MatchmakingRequest extends MatchmakingQueuePut implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userId;

	public MatchmakingRequest() {
	}

	public MatchmakingRequest(MatchmakingQueuePut other, String userId) {
		this.allowBots = other.isAllowBots();
		this.deck = other.getDeck();
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}