package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/31/17.
 */
public class LogicTest extends ServiceTest<LogicImpl> {
	private AccountsImpl accounts;
	private CardsImpl cards;
	private InventoryImpl inventory;
	private DecksImpl decks;

	@Test
	@Suspendable
	public void testStartsGame(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::startsGameSync);
	}

	private void startsGameSync() throws SuspendExecution, InterruptedException {
		// Create two players
		CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
		CreateAccountResponse player2 = accounts.createAccount("b@b.com", "b", "2");

		List<String> deckIds = new ArrayList<>();

		// Build two random decks
		for (String userId : new String[]{"1", "2"}) {
			service.initializeUser(new InitializeUserRequest().withUserId(userId));
			GetCollectionResponse collection = inventory.getCollection(new GetCollectionRequest(userId));
			Collections.shuffle(collection.getCardRecords());
			List<String> inventoryIds = collection.getCardRecords().subList(0, 30).stream().map(CardRecord::getId).collect(Collectors.toList());
			DeckCreateResponse deckCreateResponse = decks.createDeck(new DeckCreateRequest()
					.withUserId(userId)
					.withHeroClass(HeroClass.WARRIOR)
					.withName("Test Deck")
					.withInventoryIds(inventoryIds));

			deckIds.add(deckCreateResponse.getCollectionId());
		}

		StartGameResponse response = service.startGame(new StartGameRequest().withPlayers(
				new StartGameRequest.Player()
						.withUserId("1")
						.withId(0)
						.withDeckId(deckIds.get(0)),
				new StartGameRequest.Player()
						.withUserId("2")
						.withId(1)
						.withDeckId(deckIds.get(1))
		));

		for (StartGameResponse.Player player : response.getPlayers()) {
			player.getDeck().getCards().toList().forEach(c -> {
				final String cardInstanceId = (String) c.getAttribute(Attribute.CARD_INSTANCE_ID);
				getContext().assertNotNull(cardInstanceId);
			});
		}
	}

	@Test
	@Suspendable
	public void testCreatesInventory(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createInventorySync);
	}

	@Test
	@Suspendable
	public void testHandleGameEvent(TestContext context) {
		context.async().complete();
	}

	@Suspendable
	private void createInventorySync() throws SuspendExecution, InterruptedException {
		final Method awaitFiber;
		try {
			awaitFiber = Sync.class.getMethod("awaitFiber", Consumer.class);
		} catch (NoSuchMethodException e) {
			getContext().fail(e);
			return;
		}
		getContext().assertTrue(Arrays.stream(awaitFiber.getAnnotations()).anyMatch(a -> a.annotationType().equals(Suspendable.class)));
		getContext().assertTrue(Fiber.isCurrentFiber());
		final String userId = "doctorpangloss";
		CreateAccountResponse createAccountResponse = accounts.createAccount("benjamin.s.berman@gmail.com", "testpass", userId);
		getContext().assertEquals(createAccountResponse.userId, userId);

		service.initializeUser(new InitializeUserRequest().withUserId(userId));

		GetCollectionResponse response = inventory.getCollection(new GetCollectionRequest(userId));

		Set<String> cardIds = response.getCardRecords().stream().map(r -> r.getCardDesc().id).collect(Collectors.toSet());

		// Should contain the classic cards
		final DeckFormat deckFormat = new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC);
		final List<String> basicClassicCardIds = CardCatalogue.query(deckFormat).toList().stream().map(Card::getCardId).collect(Collectors.toList());
		getContext().assertTrue(cardIds.containsAll(basicClassicCardIds));
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<LogicImpl>> done) {
		accounts = new AccountsImpl().withEmbeddedConfiguration();
		cards = new CardsImpl();
		inventory = new InventoryImpl();
		decks = new DecksImpl();
		LogicImpl instance = new LogicImpl();

		vertx.deployVerticle(accounts, then -> {
			vertx.deployVerticle(cards, then2 -> {
				vertx.deployVerticle(inventory, then3 -> {
					vertx.deployVerticle(decks, then4 -> {
						vertx.deployVerticle(instance, then5 -> done.handle(new Result<>(instance)));
					});
				});
			});
		});
	}
}
