package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.cards.Card;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/19/17.
 */
public class QueryCardsResponse implements Serializable {
	private List<Card> cards;

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}

	public QueryCardsResponse withCards(final List<Card> cards) {
		this.cards = cards;
		return this;
	}

	public List<String> getCardIds() {
		return this.cards.stream().map(Card::getCardId).collect(Collectors.toList());
	}

	public void append(QueryCardsResponse other) {
		this.cards.addAll(other.getCards());
	}
}

