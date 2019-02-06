package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommand;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommandPushCardIds;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommandPushInventoryIds;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.apache.commons.lang3.RandomUtils.nextInt;

/**
 * Created by bberman on 2/16/17.
 */
public class DeckTest extends SpellsourceTestBase {

	@Test
	public void testCreateDeck(TestContext context) {
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			DeckCreateResponse deckCreateResponse = createDeckForUserId(userId1);
			GetCollectionResponse collectionResponse = getDeck(deckCreateResponse.getDeckId());
			context.assertEquals(collectionResponse.getInventoryRecords().size(), 30);
		});
	}

	@Test
	public void testCreateManyDecks(TestContext context) {
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));

			for (int i = 0; i < 100; i++) {
				context.assertEquals(getDeck(createDeckForUserId(userId1).getDeckId()).getInventoryRecords().size(), 30);
			}
		});
	}

	private GetCollectionResponse getDeck(String deckId) throws SuspendExecution, InterruptedException {
		return Inventory.getCollection(GetCollectionRequest.deck(deckId));
	}

	@Test
	public void testUpdateDecks(TestContext context) {
		// Get my card collection
		// Pick a card at random to replace
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			// Get my card collection
			GetCollectionResponse personalCollection = Inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(userId1).getDeckId();
			GetCollectionResponse deck1 = getDeck(deckId);
			// Pick a card at random to replace
			InventoryRecord replacement = personalCollection.getInventoryRecords().get(nextInt(0, personalCollection.getInventoryRecords().size()));
			InventoryRecord toReplace = deck1.getInventoryRecords().get(nextInt(0, deck1.getInventoryRecords().size()));

			Decks.updateDeck(DeckUpdateRequest.create(userId1, deckId, new DecksUpdateCommand()
					.pullAllInventoryIds(Collections.singletonList(toReplace.getId()))
					.pushInventoryIds(new DecksUpdateCommandPushInventoryIds().each(Collections.singletonList(replacement.getId())))));

			GetCollectionResponse deck2 = getDeck(deckId);
			context.assertTrue(deck2.getInventoryRecords().contains(replacement));
			context.assertFalse(deck2.getInventoryRecords().contains(toReplace));
		});
	}

	@Test
	public void testUpdateDecksWithCardIds(TestContext context) {
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId = player1.getUserId();
			CreateCollectionResponse emptyUserCollection = Inventory.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			DeckCreateResponse deck = Decks.createDeck(DeckCreateRequest.empty(userId, "name", HeroClass.BLACK));
			DeckUpdateResponse update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pushCardIds(new DecksUpdateCommandPushCardIds()
							.addEachItem("spell_mirror_image")
							.addEachItem("spell_mirror_image")
							.addEachItem("spell_mirror_image")
							.addEachItem("minion_bloodfen_raptor"))));


			context.assertEquals(4L, update.getAddedInventoryIds().stream().distinct().count());
			GetCollectionResponse userCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			context.assertEquals(3L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_mirror_image")).count());
			context.assertEquals(1L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_bloodfen_raptor")).count());
			update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pullAllCardIds(Arrays.asList("spell_mirror_image", "spell_mirror_image", "minion_bloodfen_raptor"))));
			context.assertEquals(3L, update.getRemovedInventoryIds().stream().distinct().count());
			GetCollectionResponse updatedDeck = Inventory.getCollection(GetCollectionRequest.deck(deck.getDeckId()));
			context.assertEquals(1L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_mirror_image")).count());
			context.assertEquals(0L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_bloodfen_raptor")).count());
		});
	}

	@Test
	public void testDeleteDecks(TestContext context) {
		// Get my card collection
		// Delete the deck
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			// Get my card collection
			GetCollectionResponse personalCollection = Inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(userId1).getDeckId();
			GetCollectionResponse deck1 = getDeck(deckId);
			context.assertEquals(deck1.getInventoryRecords().size(), 30);
			// Delete the deck
			DeckDeleteResponse response = Decks.deleteDeck(DeckDeleteRequest.create(deckId));
			context.assertFalse(Accounts.get(userId1).getDecks().contains(deckId));
		});
	}

	@Test
	public void testGetStandardDecks(TestContext context) {
		context.assertTrue(Spellsource.spellsource().getStandardDecks().size() > 0);
		Spellsource.spellsource().getStandardDecks().forEach(d -> context.assertEquals(30, d.getCardIds().size()));
	}
}
