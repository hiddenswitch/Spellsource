package com.hiddenswitch.spellsource.net.models;

import com.hiddenswitch.spellsource.client.models.Rarity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QueryCardsRequest implements Serializable {
	private List<QueryCardsRequest> requests;
	private String[] sets;
	private Set<CardFields> fields;
	private Rarity rarity;
	private List<String> cardIds;
	private int randomCount;

	public void setRequests(List<QueryCardsRequest> requests) {
		this.requests = requests;
	}

	public void setFields(Set<CardFields> fields) {
		this.fields = fields;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	public int getRandomCount() {
		return randomCount;
	}

	public void setRandomCount(int randomCount) {
		this.randomCount = randomCount;
	}

	public QueryCardsRequest withRequests(QueryCardsRequest... requests) {
		this.requests = Arrays.asList(requests);
		return this;
	}

	public QueryCardsRequest withSets(String... sets) {
		this.sets = sets;
		return this;
	}

	public String[] getSets() {
		return sets;
	}

	public void setSets(String[] sets) {
		this.sets = sets;
	}

	public QueryCardsRequest withFields(CardFields... fields) {
		this.fields = new HashSet<>(Arrays.asList(fields));
		return this;
	}

	public Set<CardFields> getFields() {
		return fields;
	}

	public void setFields(CardFields... fields) {
		this.fields = new HashSet<>(Arrays.asList(fields));
	}

	public boolean isBatchRequest() {
		return requests != null
				&& requests.size() > 0;
	}

	public boolean isRandomCountRequest() {
		return this.randomCount > 0;
	}

	public QueryCardsRequest withRarity(Rarity rarity) {
		this.rarity = rarity;
		return this;
	}

	public QueryCardsRequest withRandomCount(int i) {
		this.randomCount = i;
		return this;
	}

	public List<QueryCardsRequest> getRequests() {
		return requests;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}

	public QueryCardsRequest withCardIds(final List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}
}
