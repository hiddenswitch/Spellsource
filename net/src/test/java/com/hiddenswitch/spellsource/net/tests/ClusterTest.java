package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SettableFuture;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxInternal;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	 * Creates a vertx instance serving on the specified gateway port. Clusters it.
	 *
	 * @param gatewayPort
	 * @param testContext
	 * @param handler        runs the handler in the context of the newly created vertx instance
	 * @param bootstrapNodes
	 */
	@Suspendable
	private void vertx(int gatewayPort, VertxTestContext testContext, Handler<Vertx> handler, Node... bootstrapNodes) {
		GlobalTracer.registerIfAbsent(NoopTracerFactory::create);
		Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
		CardCatalogue.loadCardsFromPackage();

		var atomixInstance = new AtomicReference<Atomix>();
		var instance = Cluster.create(gatewayPort + 1, bootstrapNodes);
		atomixInstance.set(instance);
		instance.start().join();
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
	}

	@Test()
	@Timeout(95000)
	@Suspendable
	public void testMultiHostMultiClientCluster(VertxTestContext context) throws ExecutionException, InterruptedException {
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
		// Create vertx instances
		var vertxInstances = new ArrayList<Vertx>();
		// First node
		var vertxSettableFuture = new SettableFuture<Vertx>();
		vertx(ports[0], context, vertxSettableFuture::set);
		vertxInstances.add(vertxSettableFuture.get());
		// second and further nodes
		for (var i = 1; i < ports.length; i++) {
			vertxSettableFuture = new SettableFuture<>();
			vertx(ports[i], context, vertxSettableFuture::set, Node.builder().withHost("0.0.0.0").withPort(ports[0] + 1).build());
			vertxInstances.add(vertxSettableFuture.get());
		}

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
		context.awaitCompletion(95000, TimeUnit.MILLISECONDS);
	}
}
