package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.models.ValidationReport;
import com.hiddenswitch.spellsource.net.Inventory;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.models.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

@RunWith(VertxUnitRunner.class)
public class InventoryTest extends SpellsourceTestBase {

	@Test
	public void testCreateStartingCollectionAllCards(TestContext context) {
		sync(() -> {
			String userId = createRandomAccount().getUserId();
			CreateCollectionResponse response = Inventory.createCollection(CreateCollectionRequest.startingCollection(userId));
			GetCollectionResponse get = Inventory.getCollection(GetCollectionRequest.user(userId));
			context.assertEquals(response.getCreatedInventoryIds().size(), get.getInventoryRecords().size());
			context.assertEquals(Spellsource.spellsource().getStandardDecks().stream().mapToLong(s -> s.getCardIds().size()).sum(),
					(long) get.getInventoryRecords().size(), "Should have created a card for every card in the standard deck specs");
		}, context);
	}

	@Test
	public void testUpdateCollectionWithCardIds(TestContext context) {
		sync(() -> {
			CreateAccountResponse user = createRandomAccount();
			String userId = user.getUserId();
			CreateCollectionResponse createEmptyUserCollection = Inventory.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			CreateCollectionResponse createEmptyDeck = Inventory.createCollection(CreateCollectionRequest.deck(userId, "name", "TEST", Collections.emptyList(), false, new ValidationReport()));
			String deckId = createEmptyDeck.getCollectionId();
			AddToCollectionResponse addToCollectionResponse = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("minion_test_3_2", "minion_test_3_2")));
			context.assertEquals(2, addToCollectionResponse.getInventoryIds().size(), "Two 3/2s should have been added");
			GetCollectionResponse updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			GetCollectionResponse updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			Stream.of(updatedUserCollection, updatedDeckCollection)
					.forEach(res -> {
						addToCollectionResponse.getInventoryIds().forEach(ir2 -> Assert.assertTrue(res.getInventoryRecords().stream().anyMatch(ir -> ir.getId().equals(ir2))));
					});

			// Now add a card to the user collection, assert that when I request two duplicates I use one from the user
			// collection and one is created
			AddToCollectionResponse oneCardAdded = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_test_summon_tokens")));
			AddToCollectionResponse addTwoSpells = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_summon_tokens", "spell_test_summon_tokens")));
			context.assertEquals(2, addTwoSpells.getInventoryIds().size());
			context.assertEquals(1L, addTwoSpells.getInventoryIds().stream().filter(id -> id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			context.assertEquals(1L, addTwoSpells.getInventoryIds().stream().filter(id -> !id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			context.assertEquals(2L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_summon_tokens")).count());
			context.assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_summon_tokens")).count());
			context.assertEquals(4L, updatedDeckCollection.getInventoryRecords().stream().map(InventoryRecord::getId).distinct().count());

			// Now create 3 duplicate cards in the user collection, and assert that when I add 2 of that card ID, at
			// least one is unused. Then, when I add a third, assert that all 3 are being used.
			AddToCollectionResponse threeCardsAdded = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6", "spell_test_deal_6")));
			AddToCollectionResponse addTwoDeal6s = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			context.assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			AddToCollectionResponse addOneMoreDeal6 = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_test_deal_6")));

			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			context.assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(3L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Remove 2 deal6s from the deck
			RemoveFromCollectionResponse removeOneDeal6 = Inventory.removeFromCollection(RemoveFromCollectionRequest.byCardIds(deckId, Arrays.asList("spell_test_deal_6", "spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			context.assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(2L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Add a deal6 to another deck, remove it, and assert as a side effect the unchanged deck was unaffected.
			CreateCollectionResponse newDeck = Inventory.createCollection(CreateCollectionRequest.deck(userId, "name", "TEST", Collections.emptyList(), false, new ValidationReport()));
			context.assertNotEquals(newDeck.getCollectionId(), deckId);
			AddToCollectionResponse addOneDeal6ToNewDeck = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, newDeck.getCollectionId(), Arrays.asList("spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));
			context.assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			// One deal6 is in the original deck
			context.assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// One deal6 is in the new deck
			context.assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());

			RemoveFromCollectionResponse removeDeal6FromOtherDeck = Inventory.removeFromCollection(RemoveFromCollectionRequest.byCardIds(newDeck.getCollectionId(), Arrays.asList("spell_test_deal_6")));
			updatedUserCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = Inventory.getCollection(GetCollectionRequest.deck(deckId));

			context.assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			context.assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_test_deal_6")).count());
			// One deal6 remains in the original deck
			context.assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_test_deal_6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// No deal6s are in the new deck
			context.assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_deal6")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());
		}, context);
	}
}
