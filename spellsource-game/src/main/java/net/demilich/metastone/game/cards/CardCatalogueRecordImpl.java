package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.CardDesc;

/**
 * A record that stores a card's ID and {@link CardDesc}.
 */
public record CardCatalogueRecordImpl(String id, CardDesc desc) implements CardCatalogueRecord {

	/**
	 * An ID that corresponds to the file name, less the {@code .json} extension.
	 *
	 * @return
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public CardDesc getDesc() {
		return desc;
	}
}
