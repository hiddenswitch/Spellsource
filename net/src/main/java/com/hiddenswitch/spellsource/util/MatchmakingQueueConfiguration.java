package com.hiddenswitch.spellsource.util;

import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;

public class MatchmakingQueueConfiguration implements Serializable {
	private String name;
	private CardDesc[] rules;
	private int lobbySize = 2;
	private boolean botOpponent;
	private boolean ranked;
	private boolean privateLobby;
	private boolean startsAutomatically = true;
	private long stillConnectedTimeout = 2000L;
	private long emptyLobbyTimeout = 0L;
	private long awaitingLobbyTimeout = 0L;
	private boolean once;
	private boolean join;

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

	public boolean isStartsAutomatically() {
		return startsAutomatically;
	}

	public MatchmakingQueueConfiguration setStartsAutomatically(boolean startsAutomatically) {
		this.startsAutomatically = startsAutomatically;
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

	/**
	 * The amount of time a queue will await empty until it closes and destroys itself
	 *
	 * @return
	 */
	public long getEmptyLobbyTimeout() {
		return emptyLobbyTimeout;
	}

	/**
	 * When set to 0, never times out.
	 *
	 * @param emptyLobbyTimeout
	 * @return
	 */
	public MatchmakingQueueConfiguration setEmptyLobbyTimeout(long emptyLobbyTimeout) {
		this.emptyLobbyTimeout = emptyLobbyTimeout;
		return this;
	}

	/**
	 * The amount of time a queue with one or more players in it will wait for another player until it closes and destroy
	 * itself.
	 *
	 * @return
	 */
	public long getAwaitingLobbyTimeout() {
		return awaitingLobbyTimeout;
	}

	/**
	 * When set to 0, never times out.
	 *
	 * @param awaitingLobbyTimeout
	 * @return
	 */
	public MatchmakingQueueConfiguration setAwaitingLobbyTimeout(long awaitingLobbyTimeout) {
		this.awaitingLobbyTimeout = awaitingLobbyTimeout;
		return this;
	}

	public MatchmakingQueueConfiguration setOnce(boolean once) {
		this.once = once;
		return this;
	}

	/**
	 * Should the matchmaking creation invocation wait until the matchmaker is actually ready?
	 *
	 * @return
	 */
	public boolean isJoin() {
		return join;
	}

	public MatchmakingQueueConfiguration setJoin(boolean join) {
		this.join = join;
		return this;
	}
}
