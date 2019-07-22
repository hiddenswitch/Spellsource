package com.hiddenswitch.spellsource.draft;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Private information about the player's draft. This includes the actual list of cards the player will see, so it
 * should not be shared with the client.
 */
public class PrivateDraftState implements Serializable {
	private List<List<String>> cards;

	public PrivateDraftState() {
	}

	public List<List<String>> getCards() {
		return cards;
	}

	public void setCards(List<List<String>> cards) {
		this.cards = cards;
	}

	@JsonIgnore
	public Random getRandom() {
		return new Random();
	}
}
