package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.cards.CardSet;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class OpenCardPackRequest implements Serializable {
	private String userId;
	private CardSet[] sets;
	private int numberOfPacks;
	private int cardsPerPack;

	public CardSet[] getSets() {
		return sets;
	}

	public void setSets(CardSet[] sets) {
		this.sets = sets;
	}

	public int getNumberOfPacks() {
		return numberOfPacks;
	}

	public void setNumberOfPacks(int numberOfPacks) {
		this.numberOfPacks = numberOfPacks;
	}

	public int getCardsPerPack() {
		return cardsPerPack;
	}

	public void setCardsPerPack(int cardsPerPack) {
		this.cardsPerPack = cardsPerPack;
	}

	public OpenCardPackRequest withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public OpenCardPackRequest withSets(CardSet ...sets) {
		this.sets = sets;
		return this;
	}

	public OpenCardPackRequest withNumberOfPacks(int numberOfPacks) {
		this.numberOfPacks = numberOfPacks;
		return this;
	}

	public OpenCardPackRequest withCardsPerPack(int cardsPerPack) {
		this.cardsPerPack = cardsPerPack;
		return this;
	}
}
