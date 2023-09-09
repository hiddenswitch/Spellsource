package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.CardDesc;

public interface CardCatalogueRecord {
	String getId();

	CardDesc getDesc();
}
