package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.cards.CardCatalogueRecord;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public final class GetCardResponse implements Serializable {
	private CardCatalogueRecord record;

	public GetCardResponse() {
	}

	public CardCatalogueRecord getRecord() {
		return record;
	}

	public void setRecord(CardCatalogueRecord record) {
		this.record = record;
	}

	public GetCardResponse withRecord(final CardCatalogueRecord record) {
		this.record = record;
		return this;
	}
}
