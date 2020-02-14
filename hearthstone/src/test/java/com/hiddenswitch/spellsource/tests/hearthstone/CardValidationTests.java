package com.hiddenswitch.spellsource.tests.hearthstone;

import com.hiddenswitch.spellsource.testutils.CardValidation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


import java.io.File;
import java.io.IOException;

/**
 * This test will iterate through all the cards in the cards resources dir and invoke the CardParser.parseCard(cardFile)
 * method to ensure that each card is well formed and can be parsed.
 */
public class CardValidationTests extends TestBase {
	private static final String CARDS_DIR = "src/main/resources/cards/hearthstone/"; // relative path from module root

	public static Object[][] getCardFiles() {
		return CardValidation.getCardFiles(CARDS_DIR);
	}

	@ParameterizedTest
	@MethodSource("getCardFiles")
	public void validateCard(File cardFile) throws IOException {
		CardValidation.validateCard(cardFile);
	}
}
