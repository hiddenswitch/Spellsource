package net.demilich.metastone.game.cards;

import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A class responsible for deserializing JSON representations of cards.
 */
public class CardParser {

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
