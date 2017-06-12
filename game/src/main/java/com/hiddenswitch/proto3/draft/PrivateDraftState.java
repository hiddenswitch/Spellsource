package com.hiddenswitch.proto3.draft;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

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
