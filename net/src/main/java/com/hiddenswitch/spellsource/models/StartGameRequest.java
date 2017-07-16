package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bberman on 2/11/17.
 */
public class StartGameRequest implements Serializable {
	private List<Player> players;
	private String gameId;

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public StartGameRequest withPlayers(StartGameRequest.Player... players) {
		this.players = Arrays.asList(players);
		return this;
	}

	public String getGameId() {
		return gameId;
	}

	public StartGameRequest withGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	public static class Player implements Serializable {
		private String userId;
		private String deckId;
		private int id;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getDeckId() {
			return deckId;
		}

		public void setDeckId(String deckId) {
			this.deckId = deckId;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public Player withUserId(final String userId) {
			this.userId = userId;
			return this;
		}

		public Player withDeckId(final String deckId) {
			this.deckId = deckId;
			return this;
		}

		public Player withId(final int id) {
			this.id = id;
			return this;
		}
	}
}
