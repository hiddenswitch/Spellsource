package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.client.models.DecksUpdateCommand;
import com.hiddenswitch.proto3.net.client.models.DecksUpdateCommandPushInventoryIds;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomUtils.nextInt;

/**
 * Created by bberman on 2/16/17.
 */
public class DeckTest extends ServiceTest<DecksImpl> {
	private AccountsImpl accounts;
	private CardsImpl cards;
	private InventoryImpl inventory;
	private LogicImpl logic;

	public static DeckCreateResponse createDeckForUserId(Inventory inventory, Decks decks, String userId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse collection = inventory.getCollection(GetCollectionRequest.user(userId));
		Collections.shuffle(collection.getCardRecords());
		List<String> inventoryIds = collection.getCardRecords().subList(0, 30).stream().map(CardRecord::getId).collect(Collectors.toList());
		return decks.createDeck(new DeckCreateRequest()
				.withUserId(userId)
				.withHeroClass(HeroClass.WARRIOR)
				.withName("Test Deck")
				.withInventoryIds(inventoryIds));
	}

	@Test
	public void testCreateDeck(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.userId;
			LogicTest.initializeUserId(logic, userId1);
			DeckCreateResponse deckCreateResponse = createDeckForUserId(inventory, service, userId1);
			GetCollectionResponse collectionResponse = getDeck(deckCreateResponse.getCollectionId());
			getContext().assertEquals(collectionResponse.getCardRecords().size(), 30);
		});
	}

	@Test
	public void testCreateManyDecks(TestContext context) {
		setLoggingLevel(Level.ERROR);

		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.userId;
			LogicTest.initializeUserId(logic, userId1);

			for (int i = 0; i < 100; i++) {
				getContext().assertEquals(getDeck(createDeckForUserId(inventory, service, userId1).getCollectionId()).getCardRecords().size(), 30);
			}
		});
	}

	private GetCollectionResponse getDeck(String deckId) throws SuspendExecution, InterruptedException {
		return inventory.getCollection(GetCollectionRequest.deck(deckId));
	}

	@Test
	public void testUpdateDecks(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.userId;
			LogicTest.initializeUserId(logic, userId1);
			// Get my card collection
			GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(inventory, service, userId1).getCollectionId();

			GetCollectionResponse deck1 = getDeck(deckId);

			// Pick a card at random to replace
			CardRecord replacement = personalCollection.getCardRecords().get(nextInt(0, personalCollection.getCardRecords().size()));
			CardRecord toReplace = deck1.getCardRecords().get(nextInt(0, deck1.getCardRecords().size()));

			service.updateDeck(new DeckUpdateRequest(userId1, deckId, new DecksUpdateCommand()
					.pullAllInventoryIds(Collections.singletonList(toReplace.getId()))
					.pushInventoryIds(new DecksUpdateCommandPushInventoryIds().each(Collections.singletonList(replacement.getId())))));

			GetCollectionResponse deck2 = getDeck(deckId);
			getContext().assertTrue(deck2.getCardRecords().contains(replacement));
			getContext().assertFalse(deck2.getCardRecords().contains(toReplace));
		});
	}

	@Test
	public void testDeleteDecks(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.userId;
			LogicTest.initializeUserId(logic, userId1);
			// Get my card collection
			GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(inventory, service, userId1).getCollectionId();

			GetCollectionResponse deck1 = getDeck(deckId);
			getContext().assertEquals(deck1.getCardRecords().size(), 30);

			// Delete the deck
		});
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<DecksImpl>> done) {
		accounts = new AccountsImpl().withEmbeddedConfiguration();
		cards = new CardsImpl();
		inventory = new InventoryImpl();
		logic = new LogicImpl();
		DecksImpl instance = new DecksImpl();

		vertx.deployVerticle(accounts, then -> {
			vertx.deployVerticle(cards, then2 -> {
				vertx.deployVerticle(inventory, then3 -> {
					vertx.deployVerticle(logic, then4 -> {
						vertx.deployVerticle(instance, then5 -> done.handle(new Result<>(instance)));
					});
				});
			});
		});
	}
}
