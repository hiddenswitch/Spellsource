package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public abstract class SpellsourceTestBase {
	protected static AtomicBoolean initialized = new AtomicBoolean();
	protected static Vertx vertx;

	@BeforeClass
	public static void setUp() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		if (initialized.compareAndSet(false, true)) {
			CardCatalogue.loadCardsFromPackage();
			Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);

			vertx = Vertx.vertx(new VertxOptions()
					.setBlockedThreadCheckInterval(999999)
					.setBlockedThreadCheckIntervalUnit(TimeUnit.SECONDS));
			GlobalTracer.registerIfAbsent(NoopTracerFactory::create);
			vertx.runOnContext(v1 -> Spellsource.spellsource().migrate(vertx, v2 -> {
				if (v2.failed()) {
					throw new AssertionError();
				}
				Mongo.mongo().connectWithEnvironment(vertx);
				Spellsource.spellsource().deployAll(vertx, v4 -> {
					if (v4.failed()) {
						throw new AssertionError("failed");
					}
					latch.countDown();
				});
			}));
			latch.await(12000L, TimeUnit.MILLISECONDS);
		}
	}

	public static DeckCreateResponse createDeckForUserId(String userId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse collection = Inventory.getCollection(GetCollectionRequest.user(userId));
		Collections.shuffle(collection.getInventoryRecords());
		List<String> inventoryIds = collection.getInventoryRecords().subList(0, 30).stream().map(InventoryRecord::getId).collect(Collectors.toList());
		return Decks.createDeck(new DeckCreateRequest()
				.withUserId(userId)
				.withHeroClass("RED")
				.withName("Test Deck")
				.withFormat("Wild")
				.withInventoryIds(inventoryIds));
	}

	public static boolean isCI() {
		return Boolean.parseBoolean(System.getenv("CI"));
	}

	@Before
	public void setUpEach(TestContext testContext) {
		vertx.exceptionHandler(testContext.exceptionHandler());
		// Cleanup anything else that might be going on
		sync(() -> {
			for (UserId key : Matchmaking.getUsersInQueues().keySet()) {
				Matchmaking.dequeue(key);
			}

			for (GameId games : Games.getConnections().keySet()) {
				Games.endGame(games);
			}

			/*
			for (UserId connected : Connection.getConnections().keySet()) {
				Void t = awaitResult(h -> Connection.close(connected.toString(), h));
			}*/
		}, 8, testContext);
	}

	public static CreateAccountResponse createRandomAccount() throws SuspendExecution, InterruptedException {
		return Accounts.createAccount(new CreateAccountRequest().withEmailAddress("test-" + RandomStringUtils.randomAlphanumeric(32) + "@test.com")
				.withName("username" + RandomStringUtils.randomAlphanumeric(32)).withPassword("password"));
	}

	public static DefaultApi getApi() {
		DefaultApi api = new DefaultApi();
		api.setApiClient(new ApiClient());
		api.getApiClient().setBasePath(UnityClient.BASE_PATH);
		return api;
	}

	@Suspendable
	public static void sync(SuspendableRunnable action, TestContext testContext) {
		sync(action, 90, testContext);
	}

	@Suspendable
	public static void sync(SuspendableRunnable action, int seconds, TestContext testContext) {
		CountDownLatch latch = new CountDownLatch(1);
		if (Vertx.currentContext() == null) {
			vertx.runOnContext(v1 -> {
				vertx.runOnContext(suspendableHandler(v2 -> {
					try {
						action.run();
					} catch (Throwable throwable) {
						testContext.fail(throwable);
					}

					latch.countDown();
				}));
			});
		} else {
			Vertx.currentContext().runOnContext(suspendableHandler(v -> {
				try {
					action.run();
				} catch (Throwable throwable) {
					testContext.fail(throwable);
				}
			}));
		}

		try {
			latch.await(seconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			testContext.fail(e);
		}
		testContext.assertEquals(0L, latch.getCount());
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		GlobalTracer.get().close();
		CountDownLatch latch = new CountDownLatch(1);
		if (vertx == null) {
			latch.countDown();
		} else {
			vertx.close(v -> {
				initialized.compareAndSet(true, false);
				latch.countDown();
				vertx = null;
			});
		}

		latch.await(3000L, TimeUnit.MILLISECONDS);
		assertEquals(latch.getCount(), 0);
		assertNull(vertx);
	}
}
