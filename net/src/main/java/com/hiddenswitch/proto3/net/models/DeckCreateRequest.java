package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;
import java.util.List;

public class DeckCreateRequest implements Serializable {
	private String userId;
	private String name;
	private HeroClass heroClass;
	private List<String> inventoryIds;
	private List<String> cardIds;

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public DeckCreateRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public DeckCreateRequest withName(final String name) {
		this.name = name;
		return this;
	}

	public DeckCreateRequest withHeroClass(final HeroClass heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public DeckCreateRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}

	public DeckCreateRequest withCardIds(final List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}
}