package com.hiddenswitch.spellsource.net.tests.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.ClusteredGames;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.models.*;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;

@RunWith(VertxUnitRunner.class)
public abstract class SpellsourceTestBase {

	protected RunTestOnContext getTestContext() {
		return new RunTestOnContext();
	}

	@Rule
	public RunTestOnContext contextRule = getTestContext();

	@Before
	public void setUp(TestContext testContext) throws InterruptedException {
		CardCatalogue.loadCardsFromPackage();
		Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
		Vertx vertx = contextRule.vertx();
		vertx.exceptionHandler(testContext::fail);
		GlobalTracer.registerIfAbsent(NoopTracerFactory::create);

		Migrations.migrate(vertx, testContext.asyncAssertSuccess(v1 -> {
			Spellsource.spellsource().deployAll(vertx, getConcurrency(), testContext.asyncAssertSuccess());
		}));
	}

	protected int getConcurrency() {
		return Runtime.getRuntime().availableProcessors();
	}

	@After
	public void tearDown(TestContext testContext) {
	}

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

	public static boolean isCI() {
		return Boolean.parseBoolean(System.getenv("CI"));
	}

	public static CreateAccountResponse createRandomAccount() throws SuspendExecution, InterruptedException {
		return Accounts.createAccount(new CreateAccountRequest().withEmailAddress("test-" + RandomStringUtils.randomAlphanumeric(32) + "@test.com")
				.withName("username" + RandomStringUtils.randomAlphanumeric(32)).withPassword("password"));
	}

	public static DefaultApi getApi() {
		DefaultApi api = new DefaultApi();
		api.setApiClient(new ApiClient());
		api.getApiClient().setBasePath(UnityClient.BASE_PATH);
		api.getApiClient().addDefaultHeader("Accept-Encoding", "gzip");
		return api;
	}

	@Suspendable
	public void sync(SuspendableRunnable action, TestContext testContext) {
		Handler<AsyncResult<Void>> handler = testContext.asyncAssertSuccess();
		Vertx.currentContext().runOnContext(suspendableHandler(v -> {
			try {
				action.run();
				handler.handle(Future.succeededFuture());
			} catch (Throwable throwable) {
				handler.handle(Future.failedFuture(throwable));
			}
		}));
	}

	/**
	 * Brute force finds the server game context containing this user ID hosted in the current vertx instance
	 *
	 * @param eitherUserId
	 * @return
	 */
	@Suspendable
	protected Optional<ServerGameContext> getServerGameContext(UserId eitherUserId) {
		var vertx = (VertxInternal) contextRule.vertx();

		return vertx.deploymentIDs()
				.stream()
				.map(vertx::getDeployment)
				.flatMap(dep -> dep.getVerticles().stream())
				.filter(v -> v instanceof ClusteredGames)
				.map(v -> (ClusteredGames) v)
				.flatMap(cg -> cg.getContexts().values().stream())
				.filter(context -> context.getPlayerConfigurations().stream().anyMatch(c -> Objects.equals(c.getUserId(), eitherUserId)))
				.findFirst();
	}
}
