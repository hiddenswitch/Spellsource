package com.hiddenswitch.spellsource.net.tests.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SettableFuture;
import co.paralleluniverse.strands.SuspendableRunnable;
import com.hiddenswitch.containers.MongoDBContainer;
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
import io.vertx.core.impl.VertxInternal;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.core.Vertx.currentContext;

@ExtendWith(VertxExtension.class)
@Testcontainers
public abstract class SpellsourceTestBase {

	@Container
	public MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:3.6");

	@BeforeEach
	public void setUp(Vertx vertx, VertxTestContext testContext) {
		staticSetUp();

		vertx.exceptionHandler(testContext::failNow);
		Migrations.migrate(vertx, testContext.succeeding(v1 -> {
			Spellsource.spellsource().deployAll(vertx, getConcurrency(), testContext.completing());
		}));
	}

	@Suspendable
	public void staticSetUp() {
		CardCatalogue.loadCardsFromPackage();
		Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
		GlobalTracer.registerIfAbsent(NoopTracerFactory::create);
		System.getProperties().put("mongo.url", mongoDBContainer.getReplicaSetUrl());
	}

	protected int getConcurrency() {
		return Runtime.getRuntime().availableProcessors();
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

	public static CreateAccountResponse createRandomAccount() throws SuspendExecution, InterruptedException {
		return Accounts.createAccount(new CreateAccountRequest().withEmailAddress("test-" + RandomStringUtils.randomAlphanumeric(32) + "@test.com")
				.withName("username" + RandomStringUtils.randomAlphanumeric(32)).withPassword("password"));
	}

	public static CreateAccountResponse createRandomAccount(VertxTestContext testContext, Vertx vertx) throws ExecutionException, InterruptedException {
		if (Fiber.isCurrentFiber()) {
			try {
				return createRandomAccount();
			} catch (SuspendExecution suspendExecution) {
				throw new RuntimeException(suspendExecution);
			}
		} else {
			// we're in the test worker
			var response = new AtomicReference<CreateAccountResponse>();

			joinOnFiberContext(() -> {
				response.set(createRandomAccount());
			}, testContext, vertx);

			return response.get();
		}

	}

	public static DefaultApi getApi() {
		DefaultApi api = new DefaultApi();
		api.setApiClient(new ApiClient());
		api.getApiClient().setBasePath(UnityClient.BASE_PATH);
		api.getApiClient().addDefaultHeader("Accept-Encoding", "gzip");
		return api;
	}

	@Suspendable
	public void runOnFiberContext(SuspendableRunnable action, VertxTestContext testContext, Vertx vertx, Handler<AsyncResult<Void>> handler) {
		vertx.runOnContext(v -> fiber(() -> {
			try {
				action.run();
				handler.handle(Future.succeededFuture());
			} catch (Throwable throwable) {
				handler.handle(Future.failedFuture(throwable));
				testContext.failNow(throwable);
			}
		}));
	}

	@Suspendable
	public void runOnFiberContext(SuspendableRunnable action, VertxTestContext testContext, Vertx vertx) {
		runOnFiberContext(action, testContext, vertx, testContext.completing());
	}

	@Suspendable
	public static void joinOnFiberContext(SuspendableRunnable action, VertxTestContext testContext, Vertx vertx) throws ExecutionException, InterruptedException {
		var fut = new SettableFuture<Void>();
		vertx.runOnContext(v -> fiber(() -> {
			try {
				action.run();
				fut.set(null);
			} catch (Throwable throwable) {
				testContext.failNow(throwable);
				fut.setException(throwable);
			}
		}));
		fut.get();
	}

	@Suspendable
	protected void verify(VertxTestContext context, SuspendableRunnable block) {
		try {
			block.run();
		} catch (Throwable t) {
			context.failNow(t);
		}
	}

	/**
	 * Brute force finds the server game context containing this user ID hosted in the current vertx instance
	 *
	 * @param eitherUserId
	 * @return
	 */
	@Suspendable
	protected Optional<ServerGameContext> getServerGameContext(UserId eitherUserId) {
		var vertx = (VertxInternal) currentContext().owner();

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
