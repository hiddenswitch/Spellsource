package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.hiddenswitch.proto3.net.amazon.LoginToken;
import com.hiddenswitch.proto3.net.impl.AccountsImpl;
import com.hiddenswitch.proto3.net.impl.CardsImpl;
import com.hiddenswitch.proto3.net.impl.InventoryImpl;
import com.hiddenswitch.proto3.net.impl.LogicImpl;
import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.models.GetCollectionRequest;
import com.hiddenswitch.proto3.net.models.GetCollectionResponse;
import com.hiddenswitch.proto3.net.models.InitializeUserRequest;
import com.hiddenswitch.proto3.net.util.AbstractMatchmakingTest;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/31/17.
 */
public class LogicTest extends ServiceTest<LogicImpl> {
	private AccountsImpl accounts;
	private CardsImpl cards;
	private InventoryImpl inventory;

	@Test
	@Suspendable
	public void testCreatesInventory(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createInventorySync);
	}

	@Test
	@Suspendable
	public void testHandleGameEvent(TestContext context) {

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

		Set<String> cardIds = response.getInventoryRecords().stream().map(r -> r.card.getCardId()).collect(Collectors.toSet());

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
		LogicImpl instance = new LogicImpl();

		vertx.deployVerticle(accounts, then -> {
			vertx.deployVerticle(cards, then2 -> {
				vertx.deployVerticle(inventory, then3 -> {
					vertx.deployVerticle(instance, then4 -> done.handle(new Result<>(instance)));
				});
			});
		});
	}
}
