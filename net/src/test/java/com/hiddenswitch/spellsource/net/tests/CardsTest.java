package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.models.Rarity;
import com.hiddenswitch.spellsource.net.Cards;
import com.hiddenswitch.spellsource.net.models.QueryCardsRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CardsTest extends SpellsourceTestBase {

	@Test
	public void testQuery(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			// Query for a bunch of different things and compare the values
			var commons = Cards.query(new QueryCardsRequest()
					.withSets("BASIC", "CLASSIC")
					.withRarity(Rarity.COMMON)).getRecords();

			var expectedCount = CardCatalogue.query(new DeckFormat()
					.withCardSets("BASIC", "CLASSIC"), c -> c.isCollectible() && GameLogic.isRarity(c.getRarity(), Rarity.COMMON))
					.getCount();

			assertEquals(expectedCount, commons.size());
		}, context, vertx);
	}

	@Test
	public void testContainsHeroCards() {
		CardCatalogue.loadCardsFromPackage();
		for (var classCard : HeroClass.getClassCards(DeckFormat.all())) {
			assertNotNull(CardCatalogue.getCardById(classCard.getHero()));
		}
		assertNotNull(CardCatalogue.getCardById("hero_neutral"));
	}
}
