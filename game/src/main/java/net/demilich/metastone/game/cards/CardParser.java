package net.demilich.metastone.game.cards;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.hiddenswitch.spellsource.ResourceInputStream;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {

	static {
		DatabindCodec.mapper().configure(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION, true);
		DatabindCodec.mapper().configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
		DatabindCodec.mapper().registerModule(new AfterburnerModule());
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		String input = IOUtils.toString(resourceInputStream.getInputStream(), Charset.defaultCharset()).trim();
		CardDesc desc = Json.decodeValue(input, CardDesc.class);

		final String fileName = resourceInputStream.getFileName();
		String[] split = fileName.split("/");
		String id = split[split.length - 1].split("(\\.json)")[0];

		if (desc.getId() == null) {
			desc.setId(id);
		}

		// Remove tags in description
		if (desc.description != null) {
			desc.description = desc.description.replaceAll("(</?[bi]>)|\\[x\\]", "");
		}

		return new CardCatalogueRecord(id, desc);
	}
}
