package com.hiddenswitch.hearthstone;

import com.hiddenswitch.spellsource.CardValidation;
import net.demilich.metastone.tests.util.TestBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This test will iterate through all the cards in the cards resources dir and invoke the CardParser.parseCard(cardFile)
 * method to ensure that each card is well formed and can be parsed.
 */
public class CardValidationTests extends TestBase {
	private static final String CARDS_DIR = "src/main/resources/hearthstone/"; // relative path from module root

	@DataProvider(name = "CardProvider")
	public static Object[][] getCardFiles() {
		return CardValidation.getCardFiles(CARDS_DIR);
	}

	@Test(dataProvider = "CardProvider")
	public void validateCard(File cardFile) throws IOException {
		CardValidation.validateCard(cardFile);
	}
}
