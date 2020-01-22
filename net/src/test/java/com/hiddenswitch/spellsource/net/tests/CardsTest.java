package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.net.Cards;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.models.*;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class CardsTest extends SpellsourceTestBase {
	@Test
	public void testQuery(TestContext context) {
		sync(() -> {
			// Query for a bunch of different things and compare the values
			List<CardCatalogueRecord> commons = Cards.query(new QueryCardsRequest()
					.withSets("BASIC", "CLASSIC")
					.withRarity(Rarity.COMMON)).getRecords();

			int expectedCount = CardCatalogue.query(new DeckFormat()
					.withCardSets("BASIC", "CLASSIC"), c -> c.isCollectible() && c.getRarity().isRarity(Rarity.COMMON))
					.getCount();

			assertEquals(expectedCount, commons.size());
		}, context);
	}

	@Test
	public void testContainsHeroCards() {
		CardCatalogue.loadCardsFromPackage();
		for (Card classCard : HeroClass.getClassCards(DeckFormat.getFormat("All"))) {
			assertNotNull(CardCatalogue.getCardById(classCard.getHero()));
		}
	}
}
