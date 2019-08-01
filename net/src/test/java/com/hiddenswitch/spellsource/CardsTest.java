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
	public void testContainsHeroCards(TestContext context) {
		CardCatalogue.loadCardsFromPackage();
		for (Card classCard : HeroClass.getClassCards(DeckFormat.getFormat("All"))) {
			assertNotNull(CardCatalogue.getCardById(classCard.getHero()));
		}
	}


	@Test
	public void testMigration(TestContext context) {
		CardCatalogue.loadCardsFromPackage();
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));

			for (String deck : Accounts.get(userId1).getDecks()) {
				Decks.deleteDeck(DeckDeleteRequest.create(deck));
			}

			DeckCreateResponse response = Decks.createDeck(new DeckCreateRequest()
					.withUserId(userId1)
					.withHeroClass("BLUE")
					.withName("Test Deck")
					.withFormat("All")
					.withInventoryIds(new ArrayList<>(){}));

			List<String> deckIds = Accounts.get(userId1).getDecks();
			assertFalse(deckIds.isEmpty());

			String[] badHeroClasses = new String[]{"BLACK", "SILVER", "BROWN",
					"RED", "GOLD", "GREEN",
					"VIOLET", "BLUE", "WHITE"};
			deckIds = new ArrayList<>();
			for (String badHeroClass : badHeroClasses) {
				deckIds.addAll(Mongo.mongo().findWithOptions(Inventory.COLLECTIONS,
						json("heroClass", json("$regex", badHeroClass)),
						new FindOptions().setFields(json("_id", true))).stream()
						.map(o -> o.getString("_id")).collect(toList()));
			}
			for (String deckId : deckIds) {
				Decks.deleteDeck(DeckDeleteRequest.create(deckId));
			}


			deckIds = Accounts.get(userId1).getDecks();

			assertTrue(deckIds.isEmpty());


			deckIds = new ArrayList<>();
			List<String> cardIds = new ArrayList<>();

			Decks.createDeck(DeckCreateRequest.fromCardIds("TEST",
					"spell_apple").withUserId(userId1));


			deckIds = Accounts.get(userId1).getDecks();
			assertFalse(deckIds.isEmpty());

			for (String deckId : deckIds) {
				GetCollectionResponse response1 = Inventory.getCollection(new GetCollectionRequest().withDeckId(deckId));
				response1.asDeck(userId1).getCards().forEach(card -> cardIds.add(card.getCardId()));
			}

		}, context);
	}
}
