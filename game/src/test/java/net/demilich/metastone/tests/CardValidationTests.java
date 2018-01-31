package net.demilich.metastone.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.List;

import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.demilich.metastone.game.cards.CardParser;
import net.demilich.metastone.game.shared.utils.ResourceInputStream;

/**
 * This test will iterate through all the cards in the cards resources dir and invoke the CardParser.parseCard(cardFile)
 * method to ensure that each card is well formed and can be parsed.
 */
public class CardValidationTests {
	private static final String CARDS_DIR = "src/main/resources/cards/"; // relative path from module root
	private static final CardParser CARD_PARSER = new CardParser();
	private static List<File> ALL_CARD_FILES;

	@DataProvider(name = "CardProvider")
	public static Object[][] getCardFiles() {
		if (ALL_CARD_FILES == null) {
			try {
				ALL_CARD_FILES = (List<File>) FileUtils.listFiles(
						new File(CARDS_DIR),
						new RegexFileFilter("^(.*json)"),
						DirectoryFileFilter.DIRECTORY);
			} catch (IllegalArgumentException ignored) {
				ALL_CARD_FILES = (List<File>) FileUtils.listFiles(
						new File("../cards/" + CARDS_DIR),
						new RegexFileFilter("^(.*json)"),
						DirectoryFileFilter.DIRECTORY);
			}

		}

		int size = ALL_CARD_FILES.size();
		File file;
		Object[][] matrix = (Object[][]) Array.newInstance(Object.class, size, 1);
		for (int i = 0; i < size; i++) {
			file = ALL_CARD_FILES.get(i);
			matrix[i][0] = file;
		}

		return matrix;
	}

	@Test(dataProvider = "CardProvider")
	public void validateCard(File cardFile) throws FileNotFoundException {
		try {
			CardCatalogueRecord record = CARD_PARSER.parseCard(new ResourceInputStream(cardFile.getName(), new FileInputStream(cardFile), true));
			Assert.assertFalse(record.getDesc().heroClass == null && (record.getDesc().heroClasses == null || record.getDesc().heroClasses.length == 0));
			String description = record.getDesc().description;
			if (description != null) {
				AttributeMap attributes = record.getDesc().attributes;
				if (description.startsWith("Battlecry:")) {
					Assert.assertTrue(attributes != null && attributes.containsKey(Attribute.BATTLECRY), "A Battlecry card is missing a battlecry attribute.");
				}

				if (description.startsWith("Deathrattle:")) {
					Assert.assertTrue(attributes != null && attributes.containsKey(Attribute.DEATHRATTLES));
				}

			}
		} catch (Exception ex) {
			System.err.println(ex);
			Assert.fail(cardFile.getName(), ex);
		}
	}
}
