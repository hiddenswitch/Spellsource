package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;
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

	public static class Player {
		private String deckId;

		public String getDeckId() {
			return deckId;
		}

		public void setDeckId(String deckId) {
			this.deckId = deckId;
		}
	}
}
