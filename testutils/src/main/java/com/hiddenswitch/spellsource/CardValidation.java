package com.hiddenswitch.spellsource;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardParser;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

public class CardValidation {
	private static final CardParser CARD_PARSER = new CardParser();

	public static Object[][] getCardFiles(String path) {
		List<File> ALL_CARD_FILES = (List<File>) FileUtils.listFiles(
				new File(path),
				new RegexFileFilter("^(.*json)"),
				DirectoryFileFilter.DIRECTORY);

		int size = ALL_CARD_FILES.size();
		File file;
		Object[][] matrix = (Object[][]) Array.newInstance(Object.class, size, 1);
		for (int i = 0; i < size; i++) {
			file = ALL_CARD_FILES.get(i);
			matrix[i][0] = file;
		}

		return matrix;
	}

	public static void validateCard(File cardFile) throws IOException {
		ResourceInputStream resourceInputStream = new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile), true);

		try {
			CardCatalogueRecord record = CARD_PARSER.parseCard(resourceInputStream);
			Assert.assertFalse(record.getDesc().getHeroClass() == null && (record.getDesc().getHeroClasses() == null || record.getDesc().getHeroClasses().length == 0));
			String description = record.getDesc().getDescription();
			if (description != null) {
				AttributeMap attributes = record.getDesc().getAttributes();
				if (description.startsWith("Battlecry:")
						|| description.startsWith("Opener:")) {
					Assert.assertTrue(attributes != null && attributes.containsKey(Attribute.BATTLECRY),
							"An Opener card is missing the BATTLECRY attribute.");
				}

				if (description.startsWith("Deathrattle:")
						|| description.startsWith("Aftermath:")) {
					Assert.assertTrue(attributes != null && attributes.containsKey(Attribute.DEATHRATTLES),
							"An Aftermath card is missing the DEATHRATTLES attribute.");
				}

				if (record.getDesc().deathrattle != null) {
					Assert.assertNotEquals(record.getDesc().deathrattle.getTarget(), EntityReference.ADJACENT_MINIONS,
							"Deathrattles trigger from the graveyard, so they cannot contain a reference to ADJACENT_MINIONS. Use a custom.AdjacentDeathrattleSpell instead.");
				}
			}
		} catch (DecodeException ex) {
			// Decode again to throw the inner exception
			try {
				CardDesc desc = Json.mapper.readValue(new FileInputStream(cardFile), CardDesc.class);
			} catch (JsonProcessingException innerEx) {
				Assert.fail(String.format("%s has a parse exception %s, Line: %d, Column: %d",
						cardFile.getName(),
						innerEx.getMessage(),
						innerEx.getLocation().getLineNr(),
						innerEx.getLocation().getColumnNr()), innerEx);
			}
		} catch (Exception ex) {
			Assert.fail(cardFile.getName(), ex);
		}
	}
}
