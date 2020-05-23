package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;

@Execution(ExecutionMode.CONCURRENT)
public class CardCatalogueTest {
	static {
		CardCatalogue.loadCardsFromPackage();
	}

	@Test
	public void testCardCatalogueLoads() {
		Assertions.assertTrue(CardCatalogue.getAll().size() > 1000);
		Assertions.assertTrue(CardCatalogue.getBaseClasses(DeckFormat.spellsource()).size() > 5);
	}
}
