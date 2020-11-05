package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.collect.Streams;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.ClusteredGames;
import com.hiddenswitch.spellsource.net.impl.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.InitializeUserRequest;
import com.hiddenswitch.spellsource.net.models.MatchmakingRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.*;
import io.vertx.core.eventbus.impl.HandlerHolder;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.utils.ConcurrentCyclicSequence;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
public class ClusterTest extends SpellsourceTestBase {

	private static Logger LOGGER = LoggerFactory.getLogger(ClusterTest.class);
	private List<Vertx> vertxInstances;

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
		vertx(testContext, (vertx, res) -> Spellsource.spellsource(gatewayPort).deployAll(vertx, getConcurrency(), h -> res.handle(h.mapEmpty())), handler, bootstrapNodes);
	}

	@Suspendable
	private void vertx(VertxTestContext testContext, BiConsumer<Vertx, Handler<AsyncResult<Void>>> deploy, Handler<Vertx> handler, String... bootstrapNodes) {
		staticSetUp();
		Cluster.create(bootstrapNodes)
				.onComplete(testContext.succeeding(instance -> Vertx.clusteredVertx(new VertxOptions()
								.setPreferNativeTransport(true)
								.setClusterManager(instance),
						testContext.succeeding(vertx -> {
							vertx.exceptionHandler(testContext::failNow);
							Migrations.migrate(vertx, testContext.succeeding(v1 -> deploy.accept(vertx, testContext.succeeding(v2 -> {
								vertx.runOnContext(v3 -> {
									Connection.registerCodecs();
									handler.handle(vertx);
								});
							}))));
						}))));
	}

	@Test()
	@Suspendable
	public void testMultiHostMultiClientCluster(VertxTestContext context) throws ExecutionException, InterruptedException {
		System.setProperty("games.defaultNoActivityTimeout", "14000");
		var numberOfGames = 1;
		var baseRate = 1;
		var ports = new int[]{8080, 9090};
		var x = Math.max((Runtime.getRuntime().availableProcessors() / ports.length - 1) * baseRate, 1);
		// Get nearest odd number
		var count = x - (x % 2) + 1;
		// Must be odd
		assertEquals(count % 2, 1);
		var latch = context.checkpoint(count * ports.length);
		vertxInstances = getVertxes(context, ports);

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
								LOGGER.info("FLAG {}", queueNumber);
							}

							latch.flag();
							fut.complete();
						}
					}), false, context.succeeding());
				}
			}
		}, context, vertx, context.succeeding());
	}

	@AfterEach
	public void closeContexts() {
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

	@Suspendable
	@Test
	public void testGracefulHandoverOfMatchmakingQueues(VertxTestContext context) throws InterruptedException, IllegalAccessException {
		var testSetup = new TestSetup(context).invoke();
		var instanceWithoutQueue = testSetup.getInstanceWithout();
		var instanceWithQueue = testSetup.getInstanceWith();
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
			Void t = awaitResult(instanceWithQueue::close);
			Strand.sleep(8000);
			assertFalse(usersInQueues.containsKey(account.getUserId()), "should not be in queue");
			res = Matchmaking.enqueue(new MatchmakingRequest()
					.setQueueId(Matchmaking.CONSTRUCTED)
					.withDeckId(deck)
					.withUserId(account.getUserId()));
			assertTrue(res, "user should have succeeded enqueueing somewhere else due to failover of running the queue");
		}, context, instanceWithoutQueue, context.completing());
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testGracefulShutdownOfRunningGames(VertxTestContext context) throws InterruptedException, IllegalAccessException, ExecutionException {
		var instanceWithoutGame = new CompletableFuture<Vertx>();
		var instanceWithGameFuture = new CompletableFuture<Vertx>();

		vertx(context, (vertx, handler) -> {
			var p1 = Promise.<String>promise();
			var p2 = Promise.<String>promise();
			vertx.deployVerticle(new ClusteredGames(), p1);
			vertx.deployVerticle(Gateway.create(8083), p2);
			CompositeFuture.join(p1.future(), p2.future()).onComplete(h -> handler.handle(Future.succeededFuture()));
		}, instanceWithGameFuture::complete, "a", "b");

		vertx(context, (vertx, handler) -> {
			var p1 = Promise.<String>promise();
			vertx.deployVerticle(Gateway.create(9094), p1);
			p1.future().onComplete(h -> handler.handle(Future.succeededFuture()));
		}, instanceWithoutGame::complete, "a", "b");

		var vertx = instanceWithoutGame.get();
		var instanceWithGame = instanceWithGameFuture.get();
		vertxInstances = new ArrayList<>();
		vertxInstances.add(vertx);
		vertxInstances.add(instanceWithGame);
		var port = 9094;

		var checkpoint = context.checkpoint(1);
		runOnFiberContext(() -> {
			var succeeding = context.succeeding();
			String userId = awaitResult(h -> {
				vertx.executeBlocking(v -> {
					var client = new AtomicReference<UnityClient>();
					client.set(new UnityClient(context, port) {
						@Override
						protected void handleMessage(Envelope env) {
							if (env.getGame() != null && env.getGame().getServerToClient() != null && env.getGame().getServerToClient().getGameOver() != null) {
								checkpoint.flag();
								context.completeNow();
								client.get().close();
							}
							super.handleMessage(env);
						}
					});


					client.get().createUserAccount();
					client.get().ensureConnected();
					client.get().matchmakeQuickPlay(null);
					client.get().play(1);
					// call the shutdown
					h.handle(Future.succeededFuture(client.get().getAccount().getId()));
					v.complete();
				}, false, succeeding);
			});
			var inGame = Games.isInGame(new UserId(userId));
			assertTrue(inGame, "should be in game before host closed");
			Void t = awaitResult(instanceWithGame::close);
			inGame = Games.isInGame(new UserId(userId));
			assertFalse(inGame, "should not be in game now that host is closed");
			var inQueue = Matchmaking.getUsersInQueues().containsKey(userId);
			assertFalse(inQueue, "should not be in queue now that host is closed");
		}, context, vertx, context.succeeding());
	}

	@Suspendable
	private void closeAll(List<Vertx> vertxInstances) {
		if (vertxInstances == null) {
			return;
		}
		CompletableFuture.allOf(vertxInstances.stream().map(v -> {
			var fut = new CompletableFuture<>();
			v.close(i -> fut.complete(null));
			return fut;
		}).toArray(CompletableFuture[]::new)).join();
		vertxInstances.clear();
	}

	private class TestSetup {
		private VertxTestContext context;
		private Vertx instanceWithout;
		private Vertx instanceWith;
		private int instanceWithPort;
		private int[] ports;

		public TestSetup(VertxTestContext context) {
			this.context = context;
		}

		public Vertx getInstanceWithout() {
			return instanceWithout;
		}

		public Vertx getInstanceWith() {
			return instanceWith;
		}

		public int[] getPorts() {
			return ports;
		}

		public TestSetup invoke() throws IllegalAccessException {
			// setup 3 vertx instances
			this.ports = new int[]{8083, 9093, 10013};
			vertxInstances = getVertxes(context, ports);

			vertxInstances.stream()
					.map(v -> (VertxImpl) v)
					.forEach(v -> assertEquals(3, v.getClusterManager().getNodes().size()));

			// Find the instance that started the queue
			Vertx instanceWithQueue = null;
			instanceWithout = null;
			for (var vertx : vertxInstances) {
				var eb = ((ClusteredEventBus) vertx.eventBus());
				@SuppressWarnings("unchecked")
				var handlerMap = (ConcurrentMap<String, ConcurrentCyclicSequence<HandlerHolder>>) FieldUtils.readField(eb, "handlerMap", true);
				if (handlerMap.containsKey(Matchmaking.getQueueAddress("constructed"))) {
					instanceWithQueue = vertx;
				} else {
					instanceWithout = vertx;
				}
			}

			assertNotNull(instanceWithQueue);
			assertNotNull(instanceWithout);
			assertNotEquals(instanceWithout, instanceWithQueue);
			this.instanceWith = instanceWithQueue;
			var finalInstanceWithQueue = instanceWithQueue;
			this.instanceWithPort = Streams.zip(vertxInstances.stream(), Arrays.stream(this.ports).boxed(), (a, b) -> new Object[]{a, b})
					.filter(obj -> obj[0].equals(finalInstanceWithQueue))
					.map(obj -> (Integer) obj[1])
					.findFirst().orElseThrow();
			return this;
		}

		public int getInstanceWithPort() {
			return instanceWithPort;
		}
	}
}
