package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SettableFuture;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.net.impl.Sync;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.InitializeUserRequest;
import com.hiddenswitch.spellsource.net.models.MatchmakingRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxInternal;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

public class ClusterTest extends SpellsourceTestBase {

	private static Logger LOGGER = LoggerFactory.getLogger(ClusterTest.class);

	@Override
	protected int getConcurrency() {
		return Runtime.getRuntime().availableProcessors() / 2;
	}

	@Override
	public void setUp(Vertx vertx, VertxTestContext testContext) {
		testContext.completeNow();
	}

	/**
	 * Creates a single vertx instance serving on the specified gateway port. Clusters it.
	 *
	 * @param gatewayPort
	 * @param testContext
	 * @param handler        runs the handler in the context of the newly created vertx instance
	 * @param bootstrapNodes
	 */
	@Suspendable
	private void vertx(int gatewayPort, VertxTestContext testContext, Handler<Vertx> handler, Node... bootstrapNodes) {
		staticSetUp();

		var atomixInstance = new AtomicReference<Atomix>();
		var instance = Cluster.create(gatewayPort + 1, bootstrapNodes);
		atomixInstance.set(instance);

		instance.start().thenAccept(v -> {
			Vertx.clusteredVertx(new VertxOptions()
							.setPreferNativeTransport(true)
							.setClusterManager(new AtomixClusterManager(atomixInstance.get())),
					testContext.succeeding(vertx -> {
						vertx.exceptionHandler(testContext::failNow);
						Migrations.migrate(vertx, testContext.succeeding(v1 -> Spellsource.spellsource(gatewayPort).deployAll(vertx, getConcurrency(), testContext.succeeding(v2 -> {
							vertx.runOnContext(v3 -> {
								Connection.registerCodecs();
								handler.handle(vertx);
							});
						}))));
					}));
		});
	}

	@Test()
	@Suspendable
	public void testMultiHostMultiClientCluster() throws ExecutionException, InterruptedException {
		var context = new VertxTestContext();
		System.setProperty("games.defaultNoActivityTimeout", "14000");
		var numberOfGames = 1;
		var baseRate = 2;
		var ports = new int[]{8080, 9090};
		var x = Math.max((Runtime.getRuntime().availableProcessors() / ports.length - 1) * baseRate, 1);
		// Get nearest odd number
		var count = x - (x % 2) + 1;
		// Must be odd
		assertEquals(count % 2, 1);
		var latch = context.checkpoint(count * ports.length);
		var vertxInstances = getVertxes(context, ports);

		for (var vertx : vertxInstances) {
			var clusterManager = ((VertxInternal) vertx).getClusterManager();
			assertEquals(clusterManager.getNodes().size(), 2);
		}

		var vertx = vertxInstances.get(0);

		runOnFiberContext(() -> {
			var executor = vertx
					.createSharedWorkerExecutor("testers",
							count * 2,
							90000L, TimeUnit.MILLISECONDS);
			for (var i = 0; i < count; i++) {
				var queueNumber = i;
				var queueId = "private-queue-" + i;
				var queueVertx = vertxInstances.get(i % ports.length);

				queueVertx.runOnContext(v -> fiber(() ->
						Matchmaking.startMatchmaker(queueId, new MatchmakingQueueConfiguration()
								.setAwaitingLobbyTimeout(4000L)
								.setBotOpponent(false)
								.setEmptyLobbyTimeout(4000L)
								.setLobbySize(2)
								.setName(String.format("match %d", queueNumber))
								.setOnce(false)
								.setPrivateLobby(true)
								.setRanked(false)
								.setStillConnectedTimeout(2000L)
								.setJoin(true)
								.setAutomaticallyClose(true)
								.setRules(new CardDesc[0]))));

				// Give time for the queue to start
				Strand.sleep(100L);

				for (var port : ports) {
					executor.executeBlocking(fut -> verify(context, () -> {
						try (var client = new UnityClient(context, port)) {
							// Play n games in a row
							client.createUserAccount();
							for (var k = 0; k < numberOfGames; k++) {
								client.ensureConnected();
								LOGGER.trace("execute: Connected and queueing {}", k);
								client.matchmakeAndPlay(null, queueId);
								LOGGER.trace("execute: Playing {}", k);
								client.waitUntilDone();
								LOGGER.info("execute {}: Done {}", client.getUserId(), k);
								assertTrue(client.isGameOver());
								assertTrue(client.getTurnsPlayed() > 1);
							}

							latch.flag();
							fut.complete();
						}
					}), false, context.succeeding());
				}
			}
		}, context, vertx, context.succeeding());
		context.awaitCompletion(115, TimeUnit.SECONDS);
		closeAll(vertxInstances);
	}

	/**
	 * Creates a cluster of vertx instances.
	 *
	 * @param context
	 * @param ports
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@NotNull
	@Suspendable
	public List<Vertx> getVertxes(VertxTestContext context, int[] ports) {
		// Create vertx instances
		var vertxInstances = new ArrayList<CompletableFuture<Vertx>>();
		var bootstrapNodes = getBootstrapNodes(ports);

		for (var i = 0; i < ports.length; i++) {
			var fut = new CompletableFuture<Vertx>();
			vertx(ports[i], context, fut::complete,
					bootstrapNodes);

			vertxInstances.add(fut);
		}
		CompletableFuture.allOf(vertxInstances.toArray(new CompletableFuture[0])).join();
		return vertxInstances.stream().map(fut -> fut.getNow(null)).collect(toList());
	}

	@NotNull
	private Node[] getBootstrapNodes(int[] ports) {
		return Arrays.stream(ports).mapToObj(port ->
				Node.builder().withHost("0.0.0.0").withPort(port + 1).withId(Cluster.getMemberId(port + 1, "0.0.0.0")).build()).toArray(Node[]::new);
	}

	@Test()
	@Timeout(115000)
	@Suspendable
	public void testGracefulHandoverOfMatchmakingQueues() throws ExecutionException, InterruptedException {
		var context = new VertxTestContext();
		// setup 3 vertx instances
		var ports = new int[]{8083, 9093, 10013};
		var vertxInstances = getVertxes(context, ports);

		// We're going to bring down the first one because it is hosting the queue, so use the 3rd one instead to run the
		// tests
		var vertx = vertxInstances.get(2);

		runOnFiberContext(() -> {
			var account = createRandomAccount();
			var decks = Logic.initializeUser(InitializeUserRequest.create(account.getUserId()));
			var deck = decks.getDeckCreateResponses().get(0).getDeckId();
			var res = Matchmaking.enqueue(new MatchmakingRequest()
					.setQueueId(Matchmaking.CONSTRUCTED)
					.withDeckId(deck)
					.withUserId(account.getUserId()));

			assertTrue(res, "should have enqueued");
			var usersInQueues = Matchmaking.getUsersInQueues();
			assertTrue(usersInQueues.containsKey(new UserId(account.getUserId())), "should be in queue");
			Void t = awaitResult(h -> vertxInstances.get(0).close(h));
			// is this just a timing issue?
			Strand.sleep(1000);
			assertFalse(usersInQueues.containsKey(new UserId(account.getUserId())), "should not be in queue");

			res = Matchmaking.enqueue(new MatchmakingRequest()
					.setQueueId(Matchmaking.CONSTRUCTED)
					.withDeckId(deck)
					.withUserId(account.getUserId()));
			assertTrue(res, "user should have succeeded enqueueing somewhere else due to failover of running the queue");
		}, context, vertx, context.completing());
		context.awaitCompletion(30, TimeUnit.SECONDS);
		closeAll(vertxInstances);
	}

	@Suspendable
	private void closeAll(List<Vertx> vertxInstances) {
		for (var living : vertxInstances) {
			living.close();
		}
	}
}
