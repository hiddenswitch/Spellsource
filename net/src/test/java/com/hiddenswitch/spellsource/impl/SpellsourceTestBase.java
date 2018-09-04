package com.hiddenswitch.spellsource.impl;

import ch.qos.logback.classic.Level;
import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.strands.SuspendableAction1;
import com.github.fromage.quasi.strands.SuspendableRunnable;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Sync;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static org.junit.Assert.fail;

@RunWith(VertxUnitRunner.class)
public abstract class SpellsourceTestBase {
	protected static AtomicBoolean initialized = new AtomicBoolean();
	protected static HazelcastInstance hazelcastInstance;
	protected static Vertx vertx;

	@BeforeClass
	public static void setUp(TestContext context) {
		if (initialized.compareAndSet(false, true)) {
			Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
			hazelcastInstance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5701));
			final Async async = context.async();

			Vertx.clusteredVertx(new VertxOptions()
					.setClusterManager(new HazelcastClusterManager(hazelcastInstance)), context.asyncAssertSuccess(vertx -> {
				SpellsourceTestBase.vertx = vertx;
				Spellsource.spellsource().migrate(vertx, context.asyncAssertSuccess(v1 -> {
					vertx.executeBlocking(fut -> {
						Mongo.mongo().connectWithEnvironment(vertx);
						fut.complete();
					}, context.asyncAssertSuccess(v2 -> {
						Spellsource.spellsource().deployAll(vertx, context.asyncAssertSuccess(v3 -> {
							async.complete();
						}));
					}));
				}));
			}));
		}
	}

	public static DeckCreateResponse createDeckForUserId(String userId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse collection = Inventory.getCollection(GetCollectionRequest.user(userId));
		Collections.shuffle(collection.getInventoryRecords());
		List<String> inventoryIds = collection.getInventoryRecords().subList(0, 30).stream().map(InventoryRecord::getId).collect(Collectors.toList());
		return Decks.createDeck(new DeckCreateRequest()
				.withUserId(userId)
				.withHeroClass(HeroClass.RED)
				.withName("Test Deck")
				.withFormat("Wild")
				.withInventoryIds(inventoryIds));
	}

	public static boolean isCI() {
		return Boolean.parseBoolean(System.getenv("CI"));
	}

	@Before
	public void setUpEach(TestContext context) {
		vertx.exceptionHandler(context.exceptionHandler());
		// Cleanup anything else that might be going on
		sync(() -> {
			for (UserId key : Matchmaking.currentQueue().keySet()) {
				Matchmaking.dequeue(key);
			}

			for (GameId games : Games.getConnections().keySet()) {
				Games.endGame(games);
			}
		});
	}

	public static CreateAccountResponse createRandomAccount() throws SuspendExecution, InterruptedException {
		return Accounts.createAccount(new CreateAccountRequest().withEmailAddress("test-" + RandomStringUtils.randomAlphanumeric(32) + "@test.com")
				.withName("username" + RandomStringUtils.randomAlphanumeric(32)).withPassword("password"));
	}

	public static DefaultApi getApi() {
		DefaultApi api = new DefaultApi();
		api.setApiClient(new ApiClient());
		api.getApiClient().setBasePath(UnityClient.basePath);
		return api;
	}

	public static void sync(SuspendableRunnable action) {
		CountDownLatch latch = new CountDownLatch(1);
		vertx.runOnContext(v1 -> {
			vertx.runOnContext(suspendableHandler((SuspendableAction1<Void>) v2 -> {
				action.run();
				latch.countDown();
			}));
		});
		try {
			latch.await(30L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail();
		}
	}

	@AfterClass
	public static void tearDown(TestContext context) {
		// Don't shut these things down at the end.
	}
}
