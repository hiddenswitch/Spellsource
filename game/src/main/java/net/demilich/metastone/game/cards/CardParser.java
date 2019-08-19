package net.demilich.metastone.game.cards;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.CardDesc;
import com.hiddenswitch.spellsource.ResourceInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {

	static {
		Json.mapper.configure(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, true);
		Json.mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
		Json.mapper.registerModule(new AfterburnerModule());
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.getInputStream()).trim();
		CardDesc desc = Json.decodeValue(input, CardDesc.class);

		final String fileName = resourceInputStream.getFileName();
		String[] split = fileName.split("/");
		String id = split[split.length - 1].split("(\\.json)")[0];

		if (desc.getId() == null) {
			desc.setId(id);
		}

		return new CardCatalogueRecord(id, desc);
	}
}
