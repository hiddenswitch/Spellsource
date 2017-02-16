package com.hiddenswitch.proto3.net.impl.util;

import net.demilich.metastone.game.decks.Deck;

/**
 * Created by bberman on 2/15/17.
 */
public class AllianceDeck extends Deck {
	private String deckId;

	public AllianceDeck(String deckId) {
		super();
		this.deckId = deckId;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}
}
