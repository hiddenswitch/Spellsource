package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.impl.server.PregamePlayerConfiguration;
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
		private String name;

		public Player withDeck(Deck deck) {
			this.deck = deck;
			return this;
		}

		public Deck getDeck() {
			return deck;
		}

		public Player withName(final String name) {
			this.name = name;
			return this;
		}

		public String getName() {
			return name;
		}
	}

	public PregamePlayerConfiguration getPregamePlayerConfiguration1() {
		final Player p = players.get(0);
		return new PregamePlayerConfiguration(p.getDeck(), p.getName());
	}

	public PregamePlayerConfiguration getPregamePlayerConfiguration2() {
		final Player p = players.get(1);
		return new PregamePlayerConfiguration(p.getDeck(), p.getName());
	}
}
