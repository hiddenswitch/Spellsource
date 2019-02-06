package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.QueryCardsRequest;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class CardsTest extends SpellsourceTestBase {
	@Test
	public void testQuery(TestContext context) {
		sync(() -> {
			// Query for a bunch of different things and compare the values

			List<CardCatalogueRecord> commons = Cards.query(new QueryCardsRequest()
					.withSets(CardSet.BASIC, CardSet.CLASSIC)
					.withRarity(Rarity.COMMON)).getRecords();

			int expectedCount = CardCatalogue.query(new DeckFormat()
					.withCardSets(CardSet.BASIC, CardSet.CLASSIC), c -> c.isCollectible() && c.getRarity().isRarity(Rarity.COMMON))
					.getCount();

			assertEquals(expectedCount, commons.size());
		});
	}
}
