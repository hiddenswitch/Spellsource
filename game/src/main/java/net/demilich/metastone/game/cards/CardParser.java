package net.demilich.metastone.game.cards;

import com.hiddenswitch.spellsource.core.JsonConfiguration;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.desc.CardDesc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {
	static {
		JsonConfiguration.configureJson();
	}

	@SuppressWarnings("unchecked")
	public CardCatalogueRecord parseCard(ResourceInputStream resourceInputStream) throws IOException {
		var inputStream = resourceInputStream.getInputStream();
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		var input = result.toString(StandardCharsets.UTF_8);
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
