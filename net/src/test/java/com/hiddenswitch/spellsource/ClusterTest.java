package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.util.Sync;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.*;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.sync.Sync.awaitResult;

public class ClusterTest extends SpellsourceTestBase {

	private static Logger LOGGER = LoggerFactory.getLogger(ClusterTest.class);

	private static class SpellsourceInner extends Spellsource {
		SpellsourceInner() {
			super();
		}

		@Override
		protected Verticle[] services() {
			return new Verticle[]{
					Games.create(),
					Gateway.create(9090)};
		}
	}

	@Override
	protected int getConcurrency() {
		return 1;
	}

	@Override
	protected RunTestOnContext getTestContext() {
		AtomicReference<Atomix> atomixInstance = new AtomicReference<>();
		return new RunTestOnContext(() -> {
			Atomix instance = Cluster.create(5701);
			atomixInstance.set(instance);
			instance.start().join();
			CompletableFuture<Vertx> fut = new CompletableFuture<>();
			Vertx.clusteredVertx(new VertxOptions()
							.setPreferNativeTransport(true)
							.setClusterManager(new AtomixClusterManager(atomixInstance.get())),
					res -> {
						if (res.succeeded()) {
							fut.complete(res.result());
						} else {
							fut.completeExceptionally(res.cause());
						}
					});
			try {
				return fut.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}, (vertx, fut) -> {
			vertx.runOnContext(v -> {
				vertx.close(v2 -> {
					fut.accept(null);
				});
			});

		});
	}

	@Test(timeout = 95000L)
	public void testMultiHostMultiClientCluster(TestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "14000");
		// Connect to existing cluster
		int numberOfGames = 1;
		int baseRate = 1;
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * baseRate, 1);
		CountDownLatch latch = new CountDownLatch(count * 2);
		sync(() -> {
			Atomix instance = Cluster.create(5702, Node.builder().withHost("0.0.0.0").withPort(5701).build());
			try {
				Sync.get(instance.start());
				AtomixClusterManager clusterManager = new AtomixClusterManager(instance);
				Vertx vertx2 = awaitResult(h -> {
					Vertx.clusteredVertx(new VertxOptions()
							.setPreferNativeTransport(true)
							.setClusterManager(clusterManager), h);
				});
				try {
					awaitResult(h -> vertx2.runOnContext(v -> {
						Connection.registerCodecs();
						h.handle(Future.succeededFuture());
					}));

					context.assertEquals(clusterManager.getNodes().size(), 2);

					SpellsourceInner spellsourceInner = new SpellsourceInner();
					CompositeFuture res = awaitResult(h -> spellsourceInner.deployAll(vertx2, getConcurrency(), h));
					WorkerExecutor executor = contextRule.vertx()
							.createSharedWorkerExecutor("testers",
									count * 2,
									90000L, TimeUnit.MILLISECONDS);
					for (int i = 0; i < count; i++) {
						String queueId = "private-queue-" + i;
						Closeable queue = Matchmaking.startMatchmaker(queueId, new MatchmakingQueueConfiguration()
								.setAwaitingLobbyTimeout(4000L)
								.setBotOpponent(false)
								.setEmptyLobbyTimeout(4000L)
								.setLobbySize(2)
								.setName(String.format("match %d", i))
								.setOnce(false)
								.setPrivateLobby(true)
								.setRanked(false)
								.setStillConnectedTimeout(2000L)
								.setJoin(true)
								.setAutomaticallyClose(true)
								.setRules(new CardDesc[0]));

						for (int port : new int[]{8080, 9090}) {
							executor.executeBlocking(fut -> {
								try (UnityClient client = new UnityClient(context, port)) {
									// Play n games in a row
									client.createUserAccount();
									for (int k = 0; k < numberOfGames; k++) {
										client.ensureConnected();
										LOGGER.trace("execute: Connected and queueing {}", k);
										client.matchmakeAndPlay(null, queueId);
										LOGGER.trace("execute: Playing {}", k);
										client.waitUntilDone();
										LOGGER.trace("execute: Done {}", k);
										context.assertTrue(client.isGameOver());
										context.assertTrue(client.getTurnsPlayed() > 1);
									}

									latch.countDown();
									fut.complete();
								} catch (Throwable t) {
									fut.fail(t);
								}
							}, false, context.asyncAssertSuccess());
						}
					}
					latch.await();
					Strand.sleep(1000);
				} finally {
					Void t3 = awaitResult(vertx2::close);
				}
			} finally {
				Sync.get(instance.stop());
			}
		}, context);
	}
}
