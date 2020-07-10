package com.hiddenswitch.spellsource.net.impl;

import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;

/**
 * Configures a queue.
 * <p>
 * Queue configuration affects the way {@link com.hiddenswitch.spellsource.net.Matchmaking#startMatchmaker(String,
 * MatchmakingQueueConfiguration)} will create and run a queue.
 */
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
	private boolean automaticallyClose = true;

	/**
	 * The friendly, user-facing name of the queue.
	 * <p>
	 * The {@code queueId} is provided to {@link com.hiddenswitch.spellsource.net.Matchmaking#startMatchmaker(String,
	 * MatchmakingQueueConfiguration)} directly.
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	public MatchmakingQueueConfiguration setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Work in progress scenario building. Gets a set of cards that should be put into play.
	 * <p>
	 * They are cast by Player 1.
	 * <p>
	 * This can be used to specify scenarios and other custom behaviours that players can join in.
	 *
	 * @return
	 */
	public CardDesc[] getRules() {
		return rules;
	}

	public MatchmakingQueueConfiguration setRules(CardDesc[] rules) {
		this.rules = rules;
		return this;
	}

	/**
	 * The size of the lobby of this queue. Typically, this should be 2 for a multiplayer game and 1 for a single player
	 * game.
	 *
	 * @return
	 */
	public int getLobbySize() {
		return lobbySize;
	}

	public MatchmakingQueueConfiguration setLobbySize(int lobbySize) {
		this.lobbySize = lobbySize;
		return this;
	}

	/**
	 * When {@code true}, specifies that player 2 should be filled by a bot.
	 *
	 * @return
	 */
	public boolean isBotOpponent() {
		return botOpponent;
	}

	public MatchmakingQueueConfiguration setBotOpponent(boolean botOpponent) {
		this.botOpponent = botOpponent;
		return this;
	}

	/**
	 * Currently unused. Specifies the match's wins and losses should be recorded and affect the player's matchmaknig
	 * rank.
	 *
	 * @return
	 */
	public boolean isRanked() {
		return ranked;
	}

	public MatchmakingQueueConfiguration setRanked(boolean ranked) {
		this.ranked = ranked;
		return this;
	}

	/**
	 * Currently unused. Indicates this lobby should not be visible in any public listings.
	 *
	 * @return
	 */
	public boolean isPrivateLobby() {
		return privateLobby;
	}

	public MatchmakingQueueConfiguration setPrivateLobby(boolean privateLobby) {
		this.privateLobby = privateLobby;
		return this;
	}

	/**
	 * Currently unused. Specifies that the game should start as soon as enough players queue to meet the lobby size. This
	 * is currently the default behaviour and cannot be overridden.
	 *
	 * @return
	 */
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

	/**
	 * Only runs one game in this queue, then closses.
	 *
	 * @return
	 */
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
	 * <p>
	 * If the queue has been started elsewhere in the cluster, the matchmaker will be ready (i.e., it's ready to run in
	 * backup mode).
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

	/**
	 * Should the fiber be interrupted as soon as the vertx instance is closed?
	 *
	 * @return
	 */
	public boolean isAutomaticallyClose() {
		return automaticallyClose;
	}

	public MatchmakingQueueConfiguration setAutomaticallyClose(boolean automaticallyClose) {
		this.automaticallyClose = automaticallyClose;
		return this;
	}
}
