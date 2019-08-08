package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static com.hiddenswitch.spellsource.util.Sync.invoke0;
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
		AtomicReference<HazelcastInstance> hazelcastInstance = new AtomicReference<>();
		return new RunTestOnContext(() -> {
			HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getTcpDiscoverabilityConfig(5701, 5702));
			hazelcastInstance.set(instance);
			CompletableFuture<Vertx> fut = new CompletableFuture<>();
			Vertx.clusteredVertx(new VertxOptions()
							.setClusterManager(new HazelcastClusterManager(hazelcastInstance.get())),
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
				Mongo.mongo().close();
				vertx.close(v2 -> {
					hazelcastInstance.get().shutdown();
					hazelcastInstance.set(null);
					fut.accept(null);
				});
			});

		});
	}

	@Test(timeout = 90000L)
	@Ignore
	public void testMultiHostMultiClientCluster(TestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "14000");
		// Connect to existing cluster
		int numberOfGames = 2;
		int baseRate = 1;
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2 * baseRate, 2);
		CountDownLatch latch = new CountDownLatch(count);
		sync(() -> {
			HazelcastInstance instance = invoke(Hazelcast::newHazelcastInstance, Cluster.getTcpDiscoverabilityConfig(5702, 5701));
			try {
				Vertx vertx2 = awaitResult(h -> Vertx.clusteredVertx(new VertxOptions()
						.setClusterManager(new HazelcastClusterManager(instance)), h));
				try {
					awaitResult(h -> vertx2.runOnContext(v -> {
						Connection.registerCodecs();
						h.handle(Future.succeededFuture());
					}));

					SpellsourceInner spellsourceInner = new SpellsourceInner();
					CompositeFuture res = awaitResult(h -> spellsourceInner.deployAll(vertx2, getConcurrency(), h));
					WorkerExecutor executor = contextRule.vertx().createSharedWorkerExecutor("testers", count);
					for (int i = 0; i < count; i++) {
						int port;
						if (i % 2 == 1) {
							port = 9090;
						} else {
							port = 8080;
						}

						executor.executeBlocking(fut -> {
							try (UnityClient client = new UnityClient(context, port) {
								@Override
								protected int getActionIndex(ServerToClientMessage message) {

									// Always return end turn so that we end the game in a fatigue duel
									if (message.getActions().getEndTurn() != null) {
										return message.getActions().getEndTurn();
									} else {
										return super.getActionIndex(message);
									}
								}
							}) {
								// Play n games in a row
								client.createUserAccount();
								for (int j = 0; j < numberOfGames; j++) {
									LOGGER.info("execute: Queued {}", j);
									client.ensureConnected();
									client.matchmakeConstructedPlay(null);
									LOGGER.info("execute: Playing {}", j);
									client.waitUntilDone();
									LOGGER.info("execute: Done {}", j);
									context.assertTrue(client.isGameOver());
								}

								latch.countDown();
								fut.complete();
							} catch (Throwable t) {
								fut.fail(t);
							}
						}, false, context.asyncAssertSuccess());
					}
					latch.await();
					Strand.sleep(1000);
				} finally {
					Void t = awaitResult(h -> {
						vertx2.runOnContext(v -> {
							h.handle(Future.succeededFuture());
						});
					});
					t = awaitResult(vertx2::close);
				}
			} finally {
				invoke0(instance::shutdown);
			}
		}, context);
	}
}
