package com.hiddenswitch.spellsource.testutils;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.vertx.core.json.DecodeException;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.targeting.EntityReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CardValidation {
	static {
		CardCatalogue.loadCardsFromPackage();
	}

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
		ResourceInputStream resourceInputStream = new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile));

		try {
			CardCatalogueRecord record = CARD_PARSER.parseCard(resourceInputStream);
			if (record.getDesc().type != CardType.FORMAT) {
				assertFalse(record.getDesc().getHeroClass() == null && (record.getDesc().getHeroClasses() == null || record.getDesc().getHeroClasses().length == 0));
			}
			String description = record.getDesc().getDescription();
			if (description != null) {
				AttributeMap attributes = record.getDesc().getAttributes();
				if (description.startsWith("Battlecry:")
						|| description.startsWith("Opener:")) {
					assertTrue(attributes != null && attributes.containsKey(Attribute.BATTLECRY),
							"An Opener card " + cardFile.getAbsolutePath() + " is missing the BATTLECRY attribute.");
					assertNull(record.getDesc().getSpell(), "An Opener card " + cardFile.getAbsolutePath() + " has a spell specified and it probably should be an opener.");
				}

				if (description.startsWith("Deathrattle:")
						|| description.startsWith("Aftermath:")) {
					assertTrue(attributes != null && attributes.containsKey(Attribute.DEATHRATTLES),
							"An Aftermath card is missing the DEATHRATTLES attribute.");
				}

				if (record.getDesc().deathrattle != null) {
					assertNotEquals(record.getDesc().deathrattle.getTarget(), EntityReference.ADJACENT_MINIONS,
							"Deathrattles trigger from the graveyard, so they cannot contain a reference to ADJACENT_MINIONS. Use a custom.AdjacentDeathrattleSpell instead.");
				}
			}
		} catch (DecodeException ex) {
			JsonProcessingException innerEx = (JsonProcessingException) ex.getCause();// Decode again to throw the inner exception
			if (innerEx != null) {
				JsonLocation location = innerEx.getLocation() != null ? innerEx.getLocation() : new JsonLocation(cardFile, 0, 0, 0);
				fail(String.format("%s\n%s",
						cardFile.getAbsolutePath() + ":" + location.getLineNr(),
						innerEx.getMessage()));
			}
		} catch (Exception ex) {
			fail(cardFile.getName(), ex);
		}
	}

	public static void validateCardReferences(File cardFile) throws IOException {
		CardCatalogue.loadCardsFromPackage();
		ResourceInputStream resourceInputStream = new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile));

		try {
			CardCatalogueRecord record = CARD_PARSER.parseCard(resourceInputStream);
			record.getDesc().bfs().build().forEach(node -> {
				if (node.getKey().equals(SpellArg.CARD) || node.getKey().equals(EntityFilterArg.CARD)) {
					String card = (String) node.getValue();
					CardCatalogue.getCardById(card);
				} else if (node.getKey().equals(SpellArg.CARDS) || node.getKey().equals(EntityFilterArg.CARDS)) {
					if (node.getValue() instanceof String[]) {
						String[] cards = (String[]) node.getValue();
						for (String card : cards) {
							CardCatalogue.getCardById(card);
						}
					}
				}
			});
		} catch (DecodeException ex) {
			// Does not deal with this issue here
		} catch (Exception ex) {
			fail("Card " + cardFile.getAbsolutePath() + " references " + ex.getMessage() + " which cannot be found", ex);
		}
	}
}
