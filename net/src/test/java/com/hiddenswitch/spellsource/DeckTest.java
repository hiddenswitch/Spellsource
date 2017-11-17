package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommand;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommandPushInventoryIds;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
		Collections.shuffle(collection.getInventoryRecords());
		List<String> inventoryIds = collection.getInventoryRecords().subList(0, 30).stream().map(InventoryRecord::getId).collect(Collectors.toList());
		return decks.createDeck(new DeckCreateRequest()
				.withUserId(userId)
				.withHeroClass(HeroClass.RED)
				.withName("Test Deck")
				.withInventoryIds(inventoryIds));
	}

	@Test
	public void testCreateDeck(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.getUserId();
			LogicTest.initializeUserId(logic, userId1);
			DeckCreateResponse deckCreateResponse = createDeckForUserId(inventory, service, userId1);
			GetCollectionResponse collectionResponse = getDeck(deckCreateResponse.getDeckId());
			getContext().assertEquals(collectionResponse.getInventoryRecords().size(), 30);
		});
	}

	@Test
	public void testCreateManyDecks(TestContext context) {
		setLoggingLevel(Level.ERROR);

		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.getUserId();
			LogicTest.initializeUserId(logic, userId1);

			for (int i = 0; i < 100; i++) {
				getContext().assertEquals(getDeck(createDeckForUserId(inventory, service, userId1).getDeckId()).getInventoryRecords().size(), 30);
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
			final String userId1 = player1.getUserId();
			LogicTest.initializeUserId(logic, userId1);
			// Get my card collection
			GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(inventory, service, userId1).getDeckId();

			GetCollectionResponse deck1 = getDeck(deckId);

			// Pick a card at random to replace
			InventoryRecord replacement = personalCollection.getInventoryRecords().get(nextInt(0, personalCollection.getInventoryRecords().size()));
			InventoryRecord toReplace = deck1.getInventoryRecords().get(nextInt(0, deck1.getInventoryRecords().size()));

			service.updateDeck(new DeckUpdateRequest(userId1, deckId, new DecksUpdateCommand()
					.pullAllInventoryIds(Collections.singletonList(toReplace.getId()))
					.pushInventoryIds(new DecksUpdateCommandPushInventoryIds().each(Collections.singletonList(replacement.getId())))));

			GetCollectionResponse deck2 = getDeck(deckId);
			getContext().assertTrue(deck2.getInventoryRecords().contains(replacement));
			getContext().assertFalse(deck2.getInventoryRecords().contains(toReplace));
		});
	}

	@Test
	public void testDeleteDecks(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
			final String userId1 = player1.getUserId();
			LogicTest.initializeUserId(logic, userId1);
			// Get my card collection
			GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(userId1));
			String deckId = createDeckForUserId(inventory, service, userId1).getDeckId();

			GetCollectionResponse deck1 = getDeck(deckId);
			getContext().assertEquals(deck1.getInventoryRecords().size(), 30);

			// Delete the deck
			DeckDeleteResponse response = service.deleteDeck(new DeckDeleteRequest(deckId));
			getContext().assertFalse(accounts.get(userId1).getDecks().contains(deckId));
		});
	}

	@Test
	public void testGetStandardDecks(TestContext context) {
		context.assertTrue(Spellsource.spellsource().getStandardDecks().size() > 0);
		Spellsource.spellsource().getStandardDecks().forEach(d -> context.assertEquals(30, d.getCardIds().size()));
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<DecksImpl>> done) {
		accounts = new AccountsImpl();
		cards = new CardsImpl();
		inventory = new InventoryImpl();
		logic = new LogicImpl();
		DecksImpl instance = new DecksImpl();

		vertx.deployVerticle(accounts, then -> {
			vertx.deployVerticle(cards, then2 -> {
				vertx.deployVerticle(inventory, then3 -> {
					vertx.deployVerticle(logic, then4 -> {
						vertx.deployVerticle(instance, then5 -> done.handle(Future.succeededFuture(instance)));
					});
				});
			});
		});
	}
}
