package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.InitializeUserRequest;
import com.hiddenswitch.spellsource.net.models.MatchmakingRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.impl.HandlerHolder;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.utils.ConcurrentCyclicSequence;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
	private void vertx(int gatewayPort, VertxTestContext testContext, Handler<Vertx> handler, String... bootstrapNodes) {
		staticSetUp();
		Cluster.create(gatewayPort + 1, bootstrapNodes)
				.onComplete(testContext.succeeding(instance -> Vertx.clusteredVertx(new VertxOptions()
								.setPreferNativeTransport(true)
								.setEventBusOptions(new EventBusOptions().setPort(gatewayPort + 2))
								.setClusterManager(instance),
						testContext.succeeding(vertx -> {
							vertx.exceptionHandler(testContext::failNow);
							Migrations.migrate(vertx, testContext.succeeding(v1 -> Spellsource.spellsource(gatewayPort).deployAll(vertx, getConcurrency(), testContext.succeeding(v2 -> {
								vertx.runOnContext(v3 -> {
									Connection.registerCodecs();
									handler.handle(vertx);
								});
							}))));
						}))));
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
		if (context.causeOfFailure() != null) {
			fail(context.causeOfFailure());
		}
		assertTrue(context.completed());
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
			vertx(ports[i], context, fut::complete, bootstrapNodes);

			vertxInstances.add(fut);
		}
		CompletableFuture.allOf(vertxInstances.toArray(new CompletableFuture[0])).join();
		return vertxInstances.stream().map(fut -> fut.getNow(null)).collect(toList());
	}

	@NotNull
	private String[] getBootstrapNodes(int[] ports) {
		return Arrays.stream(ports).mapToObj(port ->
		{
			var hostIpAddress = Gateway.getHostIpAddress();
			return hostIpAddress + ":" + (port + 1);
		}).toArray(String[]::new);
	}

	@Test()
	@Suspendable
	public void testGracefulHandoverOfMatchmakingQueues() throws InterruptedException, IllegalAccessException {
		var context = new VertxTestContext();
		// setup 3 vertx instances
		var ports = new int[]{8083, 9093, 10013};
		var vertxInstances = getVertxes(context, ports);

		vertxInstances.stream()
				.map(v -> (VertxImpl) v)
				.forEach(v -> assertEquals(3, v.getClusterManager().getNodes().size()));

		// Find the instance that started the queue
		Vertx instanceWithQueue = null;
		Vertx instanceWithoutQueue = null;
		for (var vertx : vertxInstances) {
			var eb = ((ClusteredEventBus) vertx.eventBus());
			@SuppressWarnings("unchecked")
			var handlerMap = (ConcurrentMap<String, ConcurrentCyclicSequence<HandlerHolder>>) FieldUtils.readField(eb, "handlerMap", true);
			if (handlerMap.containsKey(Matchmaking.getQueueAddress("constructed"))) {
				instanceWithQueue = vertx;
			} else {
				instanceWithoutQueue = vertx;
			}
		}

		assertNotNull(instanceWithQueue);
		assertNotNull(instanceWithoutQueue);
		assertNotEquals(instanceWithoutQueue, instanceWithQueue);
		Vertx finalInstanceWithQueue = instanceWithQueue;
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
			assertTrue(usersInQueues.containsKey(account.getUserId()), "should be in queue");
			Void t = awaitResult(h -> finalInstanceWithQueue.close(h));
			assertFalse(usersInQueues.containsKey(account.getUserId()), "should not be in queue");

			res = Matchmaking.enqueue(new MatchmakingRequest()
					.setQueueId(Matchmaking.CONSTRUCTED)
					.withDeckId(deck)
					.withUserId(account.getUserId()));
			assertTrue(res, "user should have succeeded enqueueing somewhere else due to failover of running the queue");
		}, context, instanceWithoutQueue, context.succeeding());
		context.awaitCompletion(45, TimeUnit.SECONDS);
		if (context.causeOfFailure() != null) {
			fail(context.causeOfFailure());
		}
		assertTrue(context.completed());
		closeAll(vertxInstances);
	}

	@Test
	public void testGracefulShutdownOfRunningGames() throws InterruptedException {
		var context = new VertxTestContext();
		// setup 3 vertx instances
		var ports = new int[]{8083, 9093, 10013};
		var vertxInstances = getVertxes(context, ports);

		// We're going to bring down the first one because it is hosting the queue, so use the 3rd one instead to run the
		// tests
		var vertx = vertxInstances.get(2);
		var checkpoint = context.checkpoint(1);
		runOnFiberContext(() -> {
			String userId = awaitResult(h -> {
				vertx.executeBlocking(v -> {
					try (var client = new UnityClient(context, ports[0])) {
						// Game over is called by shutting down the server
						client.gameOverHandler(ignored -> {
							context.verify(() -> {
								assertEquals(1, client.getTurnsPlayed(), "turns played");
								checkpoint.flag();
							});
							v.complete();
						});

						client.createUserAccount();
						client.ensureConnected();
						client.matchmakeQuickPlay(null);
						client.play(1);
						// call the shutdown
						h.handle(Future.succeededFuture(client.getAccount().getId()));
					}
				}, false, context.succeeding());
			});
			assertTrue(Games.getUsersInGames().containsKey(new UserId(userId)), "should be in game before host closed");
			Void t = awaitResult(h -> vertxInstances.get(0).close(h));

			assertFalse(Games.getUsersInGames().containsKey(new UserId(userId)), "should not be in queue now that host is closed");
			assertFalse(Matchmaking.getUsersInQueues().containsKey(userId), "should not be in game now that host is closed");
		}, context, vertx, context.succeeding());

		context.awaitCompletion(30, TimeUnit.SECONDS);
		if (context.causeOfFailure() != null) {
			fail(context.causeOfFailure());
		}
		assertTrue(context.completed());
		closeAll(vertxInstances);
	}

	@Suspendable
	private void closeAll(List<Vertx> vertxInstances) {
		for (var living : vertxInstances) {
			living.close();
		}
	}
}
