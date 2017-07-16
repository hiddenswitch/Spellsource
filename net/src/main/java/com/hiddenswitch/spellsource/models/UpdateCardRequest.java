package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.cards.CardCatalogueRecord;

import java.io.Serializable;

/**
 * Created by bberman on 1/19/17.
 */
public class UpdateCardRequest implements Serializable {

	private CardCatalogueRecord card;

	public CardCatalogueRecord getCard() {
		return card;
	}

	public void setCard(CardCatalogueRecord card) {
		this.card = card;
	}
}
