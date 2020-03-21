package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.net.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.models.*;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
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
		}, context);
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
		}, context);
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
		}, context);
	}

	@Test
	public void testUpdateDecksWithCardIds(TestContext context) {
		sync(() -> {
			CreateAccountResponse player1 = createRandomAccount();
			final String userId = player1.getUserId();
			CreateCollectionResponse emptyUserCollection = Inventory.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			DeckCreateResponse deck = Decks.createDeck(DeckCreateRequest.empty(userId, "name", "TEST"));
			DeckUpdateResponse update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pushCardIds(new DecksUpdateCommandPushCardIds()
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("minion_test_3_2"))));


			context.assertEquals(4L, update.getAddedInventoryIds().stream().distinct().count());
			GetCollectionResponse userCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			context.assertEquals(3L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_test_summon_tokens")).count());
			context.assertEquals(1L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_test_3_2")).count());
			update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pullAllCardIds(Arrays.asList("spell_test_summon_tokens", "spell_test_summon_tokens", "minion_test_3_2"))));
			context.assertEquals(3L, update.getRemovedInventoryIds().stream().distinct().count());
			GetCollectionResponse updatedDeck = Inventory.getCollection(GetCollectionRequest.deck(deck.getDeckId()));
			context.assertEquals(1L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_test_summon_tokens")).count());
			context.assertEquals(0L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_test_3_2")).count());
		}, context);
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
		}, context);
	}

	@Test
	public void testGetStandardDecks(TestContext context) {
		context.assertTrue(Spellsource.spellsource().getStandardDecks().size() > 0);
		Spellsource.spellsource().getStandardDecks().forEach(d -> context.assertEquals(30, d.getCardIds().size()));
	}

	@Test
	public void testInventoryAttributesSerializeDeserializeCorrectly(TestContext context) {
		sync(() -> {
			var car = createRandomAccount();
			var deckCreateResponse = Decks.createDeck(
					DeckCreateRequest.fromCardIds(HeroClass.NAVY, CardCatalogue.getOneOneNeutralMinionCardId()).withUserId(car.getUserId()));
			var inventoryItemId = deckCreateResponse.getInventoryIds().get(0);
			var record = mongo().findOne(Inventory.COLLECTIONS, json("_id", deckCreateResponse.getCollection().getCollectionRecord().getId()), CollectionRecord.class);
			var attributes = new AttributeMap();
			attributes.put(Attribute.ARMOR, 1);
			record.setInventoryAttributes(Map.of(
					inventoryItemId, attributes
			));
			var jsonObject = json(record);
			context.assertEquals(1,
					jsonObject.getJsonObject("inventoryAttributes")
							.getJsonObject(inventoryItemId)
							.getInteger(Attribute.ARMOR.name()), "serialize correctly");

			var deserialized = jsonObject.mapTo(CollectionRecord.class);
			context.assertEquals(1, (int) deserialized.getInventoryAttributes().get(inventoryItemId).get(Attribute.ARMOR), "deserializes correctly");
		}, context);
	}

	@Test
	public void testPlayerEntityAttributeUpdate(TestContext context) {
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {
				client.createUserAccount();
				var createDeckResult = createDeckForUserId(client.getUserId().toString());
				var gameDeck = createDeckResult.getCollection().asDeck(client.getUserId().toString());
				var signatureCardId = gameDeck.getCards().get(0).getCardId();

				// Send a command to update the deck
				invoke(client.getApi()::decksUpdate, createDeckResult.getDeckId(), new DecksUpdateCommand()
						.setPlayerEntityAttribute(new DecksUpdateCommandSetPlayerEntityAttribute()
								.attribute(PlayerEntityAttributes.SIGNATURE)
								.stringValue(signatureCardId)));

				client.matchmakeQuickPlay(createDeckResult.getDeckId());
				client.play();
				var serverGameContext = getServerGameContext(client.getUserId());
				// In AI games, the player is always player zero.
				var player = serverGameContext.orElseThrow().getPlayers().stream().filter(p -> p.getUserId().equals(client.getUserId().toString())).findFirst().orElseThrow();
				context.assertEquals(signatureCardId, player.getAttribute(Attribute.SIGNATURE));
				client.waitUntilDone();

				var deck = invoke(client.getApi()::decksGet, createDeckResult.getDeckId());
				context.assertEquals(PlayerEntityAttributes.SIGNATURE, deck.getCollection().getPlayerEntityAttributes().get(0).getAttribute());
				context.assertEquals(signatureCardId, deck.getCollection().getPlayerEntityAttributes().get(0).getStringValue());
			}
		}, context);
	}
}
