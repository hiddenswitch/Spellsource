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

	public static InitializeUserResponse initializeUserId(Logic service, String userId) throws SuspendExecution, InterruptedException {
		return service.initializeUser(new InitializeUserRequest().withUserId(userId));
	}

	private void startsGameSync() throws SuspendExecution, InterruptedException {
		// Create two players

		CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
		CreateAccountResponse player2 = accounts.createAccount("b@b.com", "b", "2");

		final String userId1 = player1.userId;
		final String userId2 = player2.userId;

		List<String> deckIds = new ArrayList<>();

		// Build two random decks
		for (String userId : new String[]{userId1, userId2}) {
			initializeUserId(service, userId);
			DeckCreateResponse deckCreateResponse = DeckTest.createDeckForUserId(inventory, decks, userId);
			deckIds.add(deckCreateResponse.getCollectionId());
		}

		StartGameResponse response = service.startGame(new StartGameRequest().withPlayers(
				new StartGameRequest.Player()
						.withUserId(userId1)
						.withId(0)
						.withDeckId(deckIds.get(0)),
				new StartGameRequest.Player()
						.withUserId(userId2)
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
		CreateAccountResponse createAccountResponse = accounts.createAccount("benjamin.s.berman@gmail.com", "testpass", "doctorpangloss");
		final String userId = createAccountResponse.userId;

		service.initializeUser(new InitializeUserRequest().withUserId(userId));

		GetCollectionResponse response = inventory.getCollection(GetCollectionRequest.user(userId));

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
