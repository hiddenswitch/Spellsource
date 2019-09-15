package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static java.util.stream.Collectors.toList;
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
