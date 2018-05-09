package net.demilich.metastone.game.cards;

import java.io.IOException;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.*;
import net.demilich.metastone.game.shared.utils.ResourceInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {
	private static Logger logger = LoggerFactory.getLogger(CardParser.class);

	public static CardCatalogueRecord parseCard(JsonObject card) throws IOException {
		final String id = card.getString("id");
		CardDesc desc = card.mapTo(CardDesc.class);
		if (desc.getId() == null) {
			desc.setId(id);
		}
		return new CardCatalogueRecord(id, desc);
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.inputStream);
		CardDesc desc = Json.decodeValue(input, CardDesc.class);

		final String fileName = resourceInputStream.fileName;
		String id = fileName.split("(\\.json)")[0];

		if (desc.getId() == null) {
			desc.setId(id);
		}

		return new CardCatalogueRecord(id, desc);
	}
}
