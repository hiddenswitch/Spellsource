package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class CardCatalogueTest {

	@Test
	public void testCardCatalogueLoads() {
		CardCatalogue.loadCardsFromFilesystemDirectories("../cards/src/main/resources/cards", "../game/src/main/resources/cards");
		Assertions.assertTrue(CardCatalogue.getAll().size() > 1000);
		Assertions.assertTrue(CardCatalogue.getBaseClasses(DeckFormat.spellsource()).size() > 5);
	}
}
