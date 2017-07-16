package com.hiddenswitch.spellsource.models;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bberman on 2/11/17.
 */
public class EndGameRequest implements Serializable {
	private List<Player> players;

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public EndGameRequest withPlayers(EndGameRequest.Player... players) {
		setPlayers(Arrays.asList(players));
		return this;
	}

	public static class Player implements Serializable {
		private String deckId;

		public String getDeckId() {
			return deckId;
		}

		public void setDeckId(String deckId) {
			this.deckId = deckId;
		}

		public Player withDeckId(String deckId) {
			setDeckId(deckId);
			return this;
		}
	}
}
