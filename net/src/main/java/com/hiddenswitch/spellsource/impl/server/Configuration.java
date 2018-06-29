package com.hiddenswitch.spellsource.impl.server;

import com.hiddenswitch.spellsource.impl.UserId;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.utils.AttributeMap;

import java.io.Serializable;

public class Configuration implements Serializable, Cloneable {
	private UserId userId;
	private int playerId;
	private String name;
	private AttributeMap playerAttributes;
	private Deck deck;
	private boolean isBot;

	public UserId getUserId() {
		return userId;
	}

	public Configuration setUserId(UserId userId) {
		this.userId = userId;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Configuration setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public String getName() {
		return name;
	}

	public Configuration setName(String name) {
		this.name = name;
		return this;
	}

	public AttributeMap getPlayerAttributes() {
		return playerAttributes;
	}

	public Configuration setPlayerAttributes(AttributeMap playerAttributes) {
		this.playerAttributes = playerAttributes;
		return this;
	}

	public Deck getDeck() {
		return deck;
	}

	public Configuration setDeck(Deck deck) {
		this.deck = deck;
		return this;
	}

	public boolean isBot() {
		return isBot;
	}

	public Configuration setBot(boolean bot) {
		isBot = bot;
		return this;
	}

	@Override
	public Configuration clone() {
		try {
			Configuration clone = (Configuration)super.clone();
			clone.playerAttributes = playerAttributes.clone();
			clone.deck = deck.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
