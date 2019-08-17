package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.Sync;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.*;
import io.vertx.core.impl.VertxInternal;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2 * baseRate, 2);
		CountDownLatch latch = new CountDownLatch(count);
		sync(() -> {
			Atomix instance = Cluster.create(5702, Node.builder().withHost("localhost").withPort(5701).build());
			try {
				Sync.get(instance.start());
				Vertx vertx2 = awaitResult(h -> Vertx.clusteredVertx(new VertxOptions()
						.setPreferNativeTransport(true)
						.setClusterManager(new AtomixClusterManager(instance)), h));
				try {
					awaitResult(h -> vertx2.runOnContext(v -> {
						Connection.registerCodecs();
						h.handle(Future.succeededFuture());
					}));

					LOGGER.trace("nodes: {}", ((VertxInternal) vertx2).getClusterManager().getNodes());

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
									client.ensureConnected();
									LOGGER.trace("execute: Connected and queueing {}", j);
									client.matchmakeConstructedPlay(null);
									LOGGER.trace("execute: Playing {}", j);
									client.waitUntilDone();
									LOGGER.trace("execute: Done {}", j);
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
					Void t3 = awaitResult(vertx2::close);
				}
			} finally {
				Sync.get(instance.stop());
			}
		}, context);
	}
}
