package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.decks.Deck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bberman on 2/11/17.
 */
public class StartGameResponse implements Serializable {
	public StartGameResponse() {
		players = new ArrayList<>(Arrays.asList(null, null));
	}
	private List<StartGameResponse.Player> players;

	public List<StartGameResponse.Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public static class Player implements Serializable {
		private Deck deck;

		public Player withDeck(Deck deck) {
			this.deck = deck;
			return this;
		}

		public Deck getDeck() {
			return deck;
		}
	}
}
