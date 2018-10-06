package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.Matchmaking;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;

public class MatchmakingQueueConfiguration implements Serializable {
	private String name;
	private CardDesc[] rules;
	private int lobbySize = 2;
	private boolean botOpponent;
	private boolean ranked;
	private boolean privateLobby;
	private boolean waitsForHost;
	private long stillConnectedTimeout = 2000L;
	private boolean once;

	public String getName() {
		return name;
	}

	public MatchmakingQueueConfiguration setName(String name) {
		this.name = name;
		return this;
	}

	public CardDesc[] getRules() {
		return rules;
	}

	public MatchmakingQueueConfiguration setRules(CardDesc[] rules) {
		this.rules = rules;
		return this;
	}

	public int getLobbySize() {
		return lobbySize;
	}

	public MatchmakingQueueConfiguration setLobbySize(int lobbySize) {
		this.lobbySize = lobbySize;
		return this;
	}

	public boolean isBotOpponent() {
		return botOpponent;
	}

	public MatchmakingQueueConfiguration setBotOpponent(boolean botOpponent) {
		this.botOpponent = botOpponent;
		return this;
	}

	public boolean isRanked() {
		return ranked;
	}

	public MatchmakingQueueConfiguration setRanked(boolean ranked) {
		this.ranked = ranked;
		return this;
	}

	public boolean isPrivateLobby() {
		return privateLobby;
	}

	public MatchmakingQueueConfiguration setPrivateLobby(boolean privateLobby) {
		this.privateLobby = privateLobby;
		return this;
	}

	public boolean isWaitsForHost() {
		return waitsForHost;
	}

	public MatchmakingQueueConfiguration setWaitsForHost(boolean waitsForHost) {
		this.waitsForHost = waitsForHost;
		return this;
	}

	public long getStillConnectedTimeout() {
		return stillConnectedTimeout;
	}

	public MatchmakingQueueConfiguration setStillConnectedTimeout(long stillConnectedTimeout) {
		this.stillConnectedTimeout = stillConnectedTimeout;
		return this;
	}

	public boolean isOnce() {
		return once;
	}

	public MatchmakingQueueConfiguration setOnce(boolean once) {
		this.once = once;
		return this;
	}
}
