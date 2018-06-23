package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 12/6/16.
 */
public class MatchExpireRequest implements Serializable {
	private String gameId;
	private List<UserId> users;
	private UserId winner;

	public MatchExpireRequest(String gameId) {
		this.setGameId(gameId);
	}

	public String getGameId() {
		return gameId;
	}

	public MatchExpireRequest setGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	public List<UserId> getUsers() {
		return users;
	}

	public MatchExpireRequest setUsers(List<UserId> users) {
		this.users = users;
		return this;
	}

	public UserId getWinner() {
		return winner;
	}

	public MatchExpireRequest setWinner(UserId winner) {
		this.winner = winner;
		return this;
	}
}
