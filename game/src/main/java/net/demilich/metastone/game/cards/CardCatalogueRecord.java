package net.demilich.metastone.game.cards;

import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;

/**
 * A record that stores a card's ID, JSON representation and {@link CardDesc}.
 */
public class CardCatalogueRecord implements Serializable {
	private static final long serialVersionUID = 4740975310205862776L;
	private String id;
	private transient JsonObject cachedJson;
	private CardDesc desc;

	public CardCatalogueRecord(String id, CardDesc desc) {
		this.id = id;
		this.desc = desc;
	}

	/**
	 * An ID that corresponds to the file name, less the {@code .json} extension, in the {@link
	 * CardCatalogue#CARDS_FOLDER} in the {@code cards/src/main/resources} directory.
	 *
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CardDesc getDesc() {
		return desc;
	}

	public void setDesc(CardDesc desc) {
		this.desc = desc;
	}
}
