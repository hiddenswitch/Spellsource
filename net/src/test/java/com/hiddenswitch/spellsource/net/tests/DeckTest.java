package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;

public class DeckTest extends SpellsourceTestBase {

	/**
	 * Creates a random deck for the user.
	 *
	 * @param userId
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	public static DeckCreateResponse createDeckForUserId(String userId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse collection = Inventory.getCollection(GetCollectionRequest.user(userId));
		Collections.shuffle(collection.getInventoryRecords());
		List<String> inventoryIds = collection.getInventoryRecords().subList(0, 30).stream().map(InventoryRecord::getId).collect(Collectors.toList());
		return Decks.createDeck(new DeckCreateRequest()
				.withUserId(userId)
				.withHeroClass("TEST")
				.withName("Test Deck")
				.withFormat("All")
				.withInventoryIds(inventoryIds));
	}

	@Test
	public void testCreateDeck(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var player1 = createRandomAccount();
			var userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			var deckCreateResponse = createDeckForUserId(userId1);
			var collectionResponse = getDeck(deckCreateResponse.getDeckId());
			assertEquals(collectionResponse.getInventoryRecords().size(), 30);
		}, context, vertx);
	}

	@Test
	public void testCreateManyDecks(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var player1 = createRandomAccount();
			var userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));

			for (var i = 0; i < 100; i++) {
				assertEquals(getDeck(createDeckForUserId(userId1).getDeckId()).getInventoryRecords().size(), 30);
			}
		}, context, vertx);
	}

	private GetCollectionResponse getDeck(String deckId) throws SuspendExecution, InterruptedException {
		return Inventory.getCollection(GetCollectionRequest.deck(deckId));
	}

	@Test
	public void testUpdateDecks(Vertx vertx, VertxTestContext context) {
		// Get my card collection
		// Pick a card at random to replace
		runOnFiberContext(() -> {
			var player1 = createRandomAccount();
			var userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			// Get my card collection
			var personalCollection = Inventory.getCollection(GetCollectionRequest.user(userId1));
			var deckId = createDeckForUserId(userId1).getDeckId();
			var deck1 = getDeck(deckId);
			// Pick a card at random to replace
			var replacement = personalCollection.getInventoryRecords().get(nextInt(0, personalCollection.getInventoryRecords().size()));
			var toReplace = deck1.getInventoryRecords().get(nextInt(0, deck1.getInventoryRecords().size()));

			Decks.updateDeck(DeckUpdateRequest.create(userId1, deckId, new DecksUpdateCommand()
					.pullAllInventoryIds(Collections.singletonList(toReplace.getId()))
					.pushInventoryIds(new DecksUpdateCommandPushInventoryIds().each(Collections.singletonList(replacement.getId())))));

			var deck2 = getDeck(deckId);
			assertTrue(deck2.getInventoryRecords().contains(replacement));
			assertFalse(deck2.getInventoryRecords().contains(toReplace));
		}, context, vertx);
	}

	@Test
	public void testUpdateDecksWithCardIds(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var player1 = createRandomAccount();
			var userId = player1.getUserId();
			Inventory.createCollection(CreateCollectionRequest.emptyUserCollection(userId));
			var deck = Decks.createDeck(DeckCreateRequest.empty(userId, "name", "TEST"));
			var update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pushCardIds(new DecksUpdateCommandPushCardIds()
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("spell_test_summon_tokens")
							.addEachItem("minion_test_3_2"))));


			assertEquals(4L, update.getAddedInventoryIds().stream().distinct().count());
			var userCollection = Inventory.getCollection(GetCollectionRequest.user(userId));
			assertEquals(3L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_test_summon_tokens")).count());
			assertEquals(1L, userCollection.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_test_3_2")).count());
			update = Decks.updateDeck(DeckUpdateRequest.create(userId, deck.getDeckId(), new DecksUpdateCommand()
					.pullAllCardIds(Arrays.asList("spell_test_summon_tokens", "spell_test_summon_tokens", "minion_test_3_2"))));
			assertEquals(3L, update.getRemovedInventoryIds().stream().distinct().count());
			var updatedDeck = Inventory.getCollection(GetCollectionRequest.deck(deck.getDeckId()));
			assertEquals(1L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("spell_test_summon_tokens")).count());
			assertEquals(0L, updatedDeck.getInventoryRecords().stream().filter(ir -> ir.getCardId().equals("minion_test_3_2")).count());
		}, context, vertx);
	}

	@Test
	public void testDeleteDecks(Vertx vertx, VertxTestContext context) {
		// Get my card collection
		// Delete the deck
		runOnFiberContext(() -> {
			var player1 = createRandomAccount();
			var userId1 = player1.getUserId();
			Logic.initializeUser(InitializeUserRequest.create(userId1).withUserId(userId1));
			// Get my card collection
			var personalCollection = Inventory.getCollection(GetCollectionRequest.user(userId1));
			var deckId = createDeckForUserId(userId1).getDeckId();
			var deck1 = getDeck(deckId);
			assertEquals(deck1.getInventoryRecords().size(), 30);
			// Delete the deck
			Decks.deleteDeck(DeckDeleteRequest.create(deckId));
			assertFalse(Accounts.get(userId1).getDecks().contains(deckId));
		}, context, vertx);
	}

	@Test
	public void testGetStandardDecks() {
		assertTrue(Spellsource.spellsource().getStandardDecks().size() > 0);
		Spellsource.spellsource().getStandardDecks().forEach(d -> assertEquals(30, d.getCardIds().size()));
	}

	@Test
	public void testInventoryAttributesSerializeDeserializeCorrectly(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
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
			assertEquals(1,
					jsonObject.getJsonObject("inventoryAttributes")
							.getJsonObject(inventoryItemId)
							.getInteger(Attribute.ARMOR.name()), "serialize correctly");

			var deserialized = jsonObject.mapTo(CollectionRecord.class);
			assertEquals(1, (int) deserialized.getInventoryAttributes().get(inventoryItemId).get(Attribute.ARMOR), "deserializes correctly");
		}, context, vertx);
	}

	@Test
	public void testPlayerEntityAttributeUpdate(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				invoke0(client::createUserAccount);
				var createDeckResult = createDeckForUserId(client.getUserId().toString());
				var gameDeck = createDeckResult.getCollection().asDeck(client.getUserId().toString());
				var signatureCardId = gameDeck.getCards().get(0).getCardId();

				// Send a command to update the deck
				invoke(client.getApi()::decksUpdate, createDeckResult.getDeckId(), new DecksUpdateCommand()
						.setPlayerEntityAttribute(new DecksUpdateCommandSetPlayerEntityAttribute()
								.attribute(PlayerEntityAttributes.SIGNATURE)
								.stringValue(signatureCardId)));

				invoke0(client::matchmakeQuickPlay, createDeckResult.getDeckId());
				invoke0(client::play);
				var serverGameContext = getServerGameContext(client.getUserId());
				// In AI games, the player is always player zero.
				var player = serverGameContext.orElseThrow().getPlayers().stream().filter(p -> p.getUserId().equals(client.getUserId().toString())).findFirst().orElseThrow();
				assertEquals(signatureCardId, player.getAttribute(Attribute.SIGNATURE));
				invoke0(client::waitUntilDone);

				var deck = invoke(client.getApi()::decksGet, createDeckResult.getDeckId());
				assertEquals(PlayerEntityAttributes.SIGNATURE, deck.getCollection().getPlayerEntityAttributes().get(0).getAttribute());
				assertEquals(signatureCardId, deck.getCollection().getPlayerEntityAttributes().get(0).getStringValue());
			}
		}, context, vertx);
	}
}
