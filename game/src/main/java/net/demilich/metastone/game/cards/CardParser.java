package net.demilich.metastone.game.cards;

import java.io.IOException;
import java.util.Map;

import com.hiddenswitch.spellsource.util.Serialization;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.*;
import net.demilich.metastone.game.shared.utils.ResourceInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {
	private static Logger logger = LoggerFactory.getLogger(CardParser.class);

	public static CardCatalogueRecord parseCard(JsonObject card) throws IOException {
		// Do something horrible: Serialize to json, then read it in with GSON. :(
		String text = Json.encode(card);
		final String id = card.getString("id");
		CardDesc desc = Serialization.getGson().fromJson(text, CardDesc.class);

		if (desc.id == null) {
			desc.id = id;
		}
		return new CardCatalogueRecord(id, card, desc);
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.inputStream);
		JsonObject json = new JsonObject(Json.mapper.readValue(input, Map.class));
		CardDesc desc = Serialization.getGson().fromJson(input, CardDesc.class);

		final String fileName = resourceInputStream.fileName;
		String id = fileName.split("(\\.json)")[0];

		if (desc.id == null) {
			desc.id = id;
		}

		return new CardCatalogueRecord(id, json, desc);
	}
}
