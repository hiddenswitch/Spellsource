package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.server.Configuration;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.utils.AttributeMap;

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
		private AttributeMap attributes;
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

		public AttributeMap getAttributes() {
			return attributes;
		}

		public void setAttributes(AttributeMap attributes) {
			this.attributes = attributes;
		}

		public Player withAttributes(final AttributeMap attributes) {
			this.attributes = attributes;
			return this;
		}
	}

	public Configuration getConfig1() {
		final Player p = players.get(0);
		return new Configuration(p.getDeck(), p.getName()).withAttributes(p.attributes);
	}

	public Configuration getConfig2() {
		final Player p = players.get(1);
		return new Configuration(p.getDeck(), p.getName()).withAttributes(p.attributes);
	}
}
