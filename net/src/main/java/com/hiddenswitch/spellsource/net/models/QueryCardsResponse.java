package com.hiddenswitch.spellsource.net.models;

import net.demilich.metastone.game.cards.CardCatalogueRecord;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public final class QueryCardsResponse implements Serializable {
	private List<CardCatalogueRecord> records;

	public List<String> getCardIds() {
		return this.records.stream().map(CardCatalogueRecord::getId).collect(Collectors.toList());
	}

	public void append(QueryCardsResponse other) {
		this.records.addAll(other.getRecords());
	}

	public List<CardCatalogueRecord> getRecords() {
		return records;
	}

	public void setRecords(List<CardCatalogueRecord> records) {
		this.records = records;
	}

	public QueryCardsResponse withRecords(final List<CardCatalogueRecord> records) {
		this.records = records;
		return this;
	}
}



