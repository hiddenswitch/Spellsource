package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hiddenswitch.spellsource.client.models.Replay;
import com.hiddenswitch.spellsource.impl.GameId;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Records information about a game. Currently, this contains the replay data and is keyed by game ID.
 * <p>
 * This record is only inserted <b>after</b> a game has been completed. In the future, it will exist as soon as a game
 * has successfully started (i.e., both players have connected and sent their first messages).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRecord extends MongoRecord {
	public static final String PLAYER_USER_IDS = "playerUserIds";

	private Replay replay;
	private Date createdAt;
	private List<String> playerUserIds;
	private List<String> playerNames;
	private List<String> deckIds;
	private boolean isBotGame;

	public GameRecord(GameId gameId) {
		super(gameId.toString());
	}

	public GameRecord(String gameId) {
		super(gameId);
	}

	public Replay getReplay() {
		return replay;
	}

	public GameRecord setReplay(Replay replay) {
		this.replay = replay;
		return this;
	}

	public List<String> getPlayerUserIds() {
		return playerUserIds;
	}

	public GameRecord setPlayerUserIds(List<String> playerUserIds) {
		this.playerUserIds = playerUserIds;
		return this;
	}

	public List<String> getPlayerNames() {
		return playerNames;
	}

	public GameRecord setPlayerNames(List<String> playerNames) {
		this.playerNames = playerNames;
		return this;
	}

	public List<String> getDeckIds() {
		return deckIds;
	}

	public GameRecord setDeckIds(List<String> deckIds) {
		this.deckIds = deckIds;
		return this;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public GameRecord setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public boolean isBotGame() {
		return isBotGame;
	}

	public GameRecord setBotGame(boolean botGame) {
		isBotGame = botGame;
		return this;
	}
}
