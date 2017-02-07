package net.demilich.metastone.game.cards;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bberman on 2/7/17.
 */
public class CardCatalogueRecord implements Serializable {
	private String id;
	private String json;
	private transient JsonObject cachedJson;
	private CardDesc desc;

	public CardCatalogueRecord(String id, JsonObject json, CardDesc desc) {
		this.id = id;
		this.json = json.encode();
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	public JsonObject getJson() {
		if (cachedJson == null) {
			cachedJson = new JsonObject(Json.decodeValue(json, Map.class));
		}
		return cachedJson;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public CardDesc getDesc() {
		return desc;
	}

	public void setDesc(CardDesc desc) {
		this.desc = desc;
	}
}
