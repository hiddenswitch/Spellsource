package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;

/**
 * A record that stores a card's ID and {@link CardDesc}.
 */
public record CardCatalogueRecord(String id, CardDesc desc) {

	/**
	 * An ID that corresponds to the file name, less the {@code .json} extension.
	 *
	 * @return
	 */
	public String getId() {
		return id;
	}

	public CardDesc getDesc() {
		return desc;
	}
}
