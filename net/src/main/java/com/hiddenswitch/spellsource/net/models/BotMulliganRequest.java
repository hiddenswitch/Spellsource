package com.hiddenswitch.spellsource.net.models;

import net.demilich.metastone.game.cards.Card;

import java.io.Serializable;
import java.util.List;

public class BotMulliganRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Card> cards;

	public BotMulliganRequest(List<Card> cards) {
		this.cards = cards;
	}
}
