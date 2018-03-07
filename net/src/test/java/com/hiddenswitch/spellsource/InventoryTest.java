package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.CardsImpl;
import com.hiddenswitch.spellsource.impl.InventoryImpl;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by bberman on 1/22/17.
 */
@RunWith(VertxUnitRunner.class)
public class InventoryTest extends ServiceTest<InventoryImpl> {

	@Test
	public void testCreateStartingCollectionAllCards(TestContext context) {
		wrapSync(context, () -> {
			final String userId = "userId";
			CreateCollectionResponse response = service.createCollection(CreateCollectionRequest.startingCollection(userId));
			GetCollectionResponse get = service.getCollection(GetCollectionRequest.user(userId));
			getContext().assertEquals(response.getCreatedInventoryIds().size(), get.getInventoryRecords().size());
			getContext().assertEquals(Spellsource.spellsource().getStandardDecks().stream().mapToLong(s -> s.getCardIds().size()).sum(),
					(long) get.getInventoryRecords().size(), "Should have created a card for every card in the standard deck specs");
		});
	}

	@Test
	public void testUpdateCollectionWithCardIds(TestContext context) {
		wrapSync(context, () -> {
			final String userId = "userId";
			CreateCollectionResponse createEmptyUserCollection = service.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			CreateCollectionResponse createEmptyDeck = service.createCollection(CreateCollectionRequest.deck(userId, "name", HeroClass.BLACK, Collections.emptyList(), false));
			final String deckId = createEmptyDeck.getCollectionId();
			AddToCollectionResponse addToCollectionResponse = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("minion_bloodfen_raptor", "minion_bloodfen_raptor")));
			getContext().assertEquals(2, addToCollectionResponse.getInventoryIds().size(), "Two Bloodfen Raptors should have been added");
			GetCollectionResponse updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			GetCollectionResponse updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			Stream.of(updatedUserCollection, updatedDeckCollection)
					.forEach(res -> {
						addToCollectionResponse.getInventoryIds().forEach(ir2 -> Assert.assertTrue(res.getInventoryRecords().stream().anyMatch(ir -> ir.getId().equals(ir2))));
					});

			// Now add a card to the user collection, assert that when I request two duplicates I use one from the user
			// collection and one is created
			AddToCollectionResponse oneCardAdded = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_mirror_image")));
			AddToCollectionResponse addTwoMirrorImages = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_mirror_image", "spell_mirror_image")));
			getContext().assertEquals(2, addTwoMirrorImages.getInventoryIds().size());
			getContext().assertEquals(1L, addTwoMirrorImages.getInventoryIds().stream().filter(id -> id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			getContext().assertEquals(1L, addTwoMirrorImages.getInventoryIds().stream().filter(id -> !id.equals(oneCardAdded.getInventoryIds().get(0))).count());
			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			getContext().assertEquals(2L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_mirror_image")).count());
			getContext().assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_mirror_image")).count());
			getContext().assertEquals(4L, updatedDeckCollection.getInventoryRecords().stream().map(InventoryRecord::getId).distinct().count());

			// Now create 3 duplicate cards in the user collection, and assert that when I add 2 of that card ID, at
			// least one is unused. Then, when I add a third, assert that all 3 are being used.
			AddToCollectionResponse threeCardsAdded = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, userId, Arrays.asList("spell_fireball", "spell_fireball", "spell_fireball")));
			AddToCollectionResponse addTwoFireballs = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_fireball", "spell_fireball")));
			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			getContext().assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(2L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			AddToCollectionResponse addOneMoreFireball = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, Arrays.asList("spell_fireball")));

			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			getContext().assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(3L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Remove 2 fireballs from the deck
			RemoveFromCollectionResponse removedOneFireball = service.removeFromCollection(RemoveFromCollectionRequest.byCardIds(deckId, Arrays.asList("spell_fireball", "spell_fireball")));
			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			getContext().assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(2L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().noneMatch(cid -> cid.equals(deckId))).count());

			// Add a fireball to another deck, remove it, and assert as a side effect the unchanged deck was unaffected.
			CreateCollectionResponse newDeck = service.createCollection(CreateCollectionRequest.deck(userId, "name", HeroClass.BLACK, Collections.emptyList(), false));
			getContext().assertNotEquals(newDeck.getCollectionId(), deckId);
			AddToCollectionResponse addOneFireballToNewDeck = service.addToCollection(AddToCollectionRequest.createWithCardIds(userId, newDeck.getCollectionId(), Arrays.asList("spell_fireball")));
			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));
			getContext().assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			// One fireball is in the original deck
			getContext().assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// One fireball is in the new deck
			getContext().assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());

			RemoveFromCollectionResponse removeFireballFromOtherDeck = service.removeFromCollection(RemoveFromCollectionRequest.byCardIds(newDeck.getCollectionId(), Arrays.asList("spell_fireball")));
			updatedUserCollection = service.getCollection(GetCollectionRequest.user(userId));
			updatedDeckCollection = service.getCollection(GetCollectionRequest.deck(deckId));

			getContext().assertEquals(3L, updatedUserCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			getContext().assertEquals(1L, updatedDeckCollection.getInventoryRecords().stream().filter(records -> records.getCardId().equals("spell_fireball")).count());
			// One fireball remains in the original deck
			getContext().assertEquals(1L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(deckId))).count());
			// No fireballs are in the new deck
			getContext().assertEquals(0L,
					updatedUserCollection.getInventoryRecords().stream().filter(record -> record.getCardId().equals("spell_fireball")
							&& record.getCollectionIds().stream().anyMatch(cid -> cid.equals(newDeck.getCollectionId()))).count());
		});
	}


	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<InventoryImpl>> done) {
		deploy(Arrays.asList(new CardsImpl()), new InventoryImpl(), done);
	}
}
