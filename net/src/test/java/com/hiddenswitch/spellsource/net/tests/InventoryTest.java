package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.models.ValidationReport;
import com.hiddenswitch.spellsource.net.Inventory;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.models.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest extends SpellsourceTestBase {

	@Test
	public void testCreateStartingCollectionAllCards(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var userId = createRandomAccount().getUserId();
			var response = Inventory.createCollection(CreateCollectionRequest.startingCollection(userId));
			var get = Inventory.getCollection(GetCollectionRequest.user(userId));
			assertEquals(response.getCreatedInventoryIds().size(), get.getInventoryRecords().size());
			assertEquals(Spellsource.spellsource().getStandardDecks().stream().mapToLong(s -> s.getCardIds().size()).sum(),
					(long) get.getInventoryRecords().size(), "Should have created a card for every card in the standard deck specs");
		}, context, vertx);
	}

	@Test
	public void testUpdateCollectionWithCardIds(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var user = createRandomAccount();
			var userId = user.getUserId();
			Inventory.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			var createEmptyDeck = Inventory.createCollection(CreateCollectionRequest.deck(userId, "name", "TEST", Collections.emptyList(), false, new ValidationReport()));
			var deckId = createEmptyDeck.getCollectionId();
			var addToCollectionResponse = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("minion_test_3_2", "minion_test_3_2")));
			assertEquals(2, addToCollectionResponse.getInventoryIds().size(), "Two 3/2s should have been added");
			var updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			var updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			Stream.of(updatedUserCollection, updatedDeckCollection)
					.forEach(res -> addToCollectionResponse.getInventoryIds().forEach(ir2 -> verify(context, ()->assertTrue(res.getInventoryRecords().stream().anyMatch(ir -> ir.getId().equals(ir2))))));

			// Now add a card to the user collection, assert that when I request two duplicates I use one from the user
			// collection and one is created
			var oneCardAdded = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_test_summon_tokens")));
			var addTwoSpells = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_summon_tokens", "spell_test_summon_tokens")));
			assertEquals(2, addTwoSpells.getInventoryIds().size());
			assertEquals(1L, addTwoSpells.getInventoryIds().stream().filter(id -> id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			assertEquals(1L, addTwoSpells.getInventoryIds().stream().filter(id -> !id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			assertEquals(2L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_summon_tokens")).count());
			assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_summon_tokens")).count());
			assertEquals(4L, updatedDeckCollection.getInventoryRecords().stream().map(InventoryRecord::getId).distinct().count());

			// Now create 3 duplicate cards in the user collection, and assert that when I add 2 of that card ID, at
			// least one is unused. Then, when I add a third, assert that all 3 are being used.
			var threeCardsAdded = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6", "spell_test_deal_6")));
			var addTwoDeal6s = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			var addOneMoreDeal6 = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_deal_6")));

			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(3L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Remove 2 deal6s from the deck
			var removeOneDeal6 = Inventory.removeFromCollection(RemoveFromCollectionRequest.byCardIds(deckId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(2L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Add a deal6 to another deck, remove it, and assert as a side effect the unchanged deck was unaffected.
			var newDeck = Inventory.createCollection(CreateCollectionRequest.deck(userId, "name", "TEST", Collections.emptyList(), false, new ValidationReport()));
			assertNotEquals(newDeck.getCollectionId(), deckId);
			var addOneDeal6ToNewDeck = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, newDeck.getCollectionId(), Arrays.asList("spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			// One deal6 is in the original deck
			assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// One deal6 is in the new deck
			assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());

			var removeDeal6FromOtherDeck = Inventory.removeFromCollection(RemoveFromCollectionRequest.byCardIds(newDeck.getCollectionId(), Arrays.asList("spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));

			assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			// One deal6 remains in the original deck
			assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// No deal6s are in the new deck
			assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_deal6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());
		}, context, vertx);
	}
}
