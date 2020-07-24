package com.hiddenswitch.spellsource.net.models;

import java.io.Serializable;

public class OpenCardPackRequest implements Serializable {
	private String userId;
	private String[] sets;
	private int numberOfPacks;
	private int cardsPerPack;

	public String[] getSets() {
		return sets;
	}

	public void setSets(String[] sets) {
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

	public OpenCardPackRequest withSets(String ...sets) {
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
