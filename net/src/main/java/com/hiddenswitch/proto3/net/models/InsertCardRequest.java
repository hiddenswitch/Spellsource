package com.hiddenswitch.proto3.net.models;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.CardCatalogueRecord;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class InsertCardRequest implements Serializable {
	private CardCatalogueRecord card;

	public CardCatalogueRecord getCard() {
		return card;
	}

	public void setCard(CardCatalogueRecord card) {
		this.card = card;
	}

	public InsertCardRequest withCard(CardCatalogueRecord card) {
		this.card = card;
		return this;
	}
}
