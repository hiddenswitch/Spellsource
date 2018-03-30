package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.*;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.util.Logging.setLoggingLevel;

@RunWith(VertxUnitRunner.class)
public class ClusterTest {
	private Logger logger = LoggerFactory.getLogger(ClusterTest.class);
	private List<Vertx> verticies = new ArrayList<>();
	private List<HazelcastInstance> hazelcastInstances = new ArrayList<>();
	private static final int blockedThreadCheckInterval = (int) Duration.of(8, ChronoUnit.SECONDS).toMillis();
	private static final long timeoutMillis = Duration.of(55, ChronoUnit.SECONDS).toMillis();

	@Test(timeout = 155000L)
	@Ignore
	@SuppressWarnings("unchecked")
	public void testClusteredDeploy(TestContext context) {
		if (ServiceTest.isCI()) {
			return;
		}
		// For debug purposes
		Async async = context.async(5);
		final Handler<AsyncResult<CompositeFuture>> handler1 = asyncAssertSuccess(context, async);

		final Handler<AsyncResult<Vertx>> handler0 = asyncAssertSuccess(context, async, vertx1 -> {
			verticies.add(vertx1);
			vertx1.exceptionHandler(context.exceptionHandler());
			final List<Future> services = Stream.of(
					new ClusteredGamesImpl(),
					new BotsImpl(),
					new LogicImpl())
					.map(service -> {
						Future<String> future = Future.future();
						vertx1.deployVerticle(service, future);
						return (Future) future;
					}).collect(Collectors.toList());

			CompositeFuture.all(services).setHandler(handler1);
		});

		setLoggingLevel(Level.ERROR);
		ClusterManager clusterManager1 = new HazelcastClusterManager(hazelcastInstances.get(0));

		VertxOptions options1 = new VertxOptions()
				.setClusterManager(clusterManager1)
				.setBlockedThreadCheckInterval(blockedThreadCheckInterval);
		// Test instance. nothing special here
		Vertx vertxTest = Vertx.vertx();
		verticies.add(vertxTest);

		// Instance 1, deploy games, AI and logic
		Vertx.clusteredVertx(options1, handler0);

		ClusterManager clusterManager2 = new HazelcastClusterManager(hazelcastInstances.get(1));
		VertxOptions options2 = new VertxOptions()
				.setClusterManager(clusterManager2)
				.setBlockedThreadCheckInterval(blockedThreadCheckInterval);

		final Handler<AsyncResult<Object>> handler4 = asyncAssertSuccess(context, async);
		final Handler<AsyncResult<CompositeFuture>> handler3 = asyncAssertSuccess(context, async, then2 -> {
			// Create a unity client and try to finish a game.
			vertxTest.executeBlocking(done -> {
				UnityClient client = new UnityClient(context);
				client.createUserAccount(null);
				client.matchmakeAndPlayAgainstAI(null);
				client.waitUntilDone();
				context.assertTrue(client.isGameOver());
				done.complete();
			}, handler4);
		});

		final Handler<AsyncResult<Vertx>> handler2 = asyncAssertSuccess(context, async, vertx2 -> {
			verticies.add(vertx2);
			vertx2.exceptionHandler(context.exceptionHandler());
			final List<Future> gameServices = Stream.of(
					new AccountsImpl(),
					new CardsImpl(),
					new DecksImpl(),
					new DraftImpl(),
					new InventoryImpl(),
					new MatchmakingImpl(),
					new GatewayImpl())
					.map(service -> {
						Future<String> future = Future.future();
						vertx2.deployVerticle(service, future);
						return (Future) future;
					}).collect(Collectors.toList());

			CompositeFuture.all(gameServices).setHandler(handler3);
		});

		// Instance 2, deploy all the supporting services
		Vertx.clusteredVertx(options2, handler2);

		async.awaitSuccess(timeoutMillis);
	}

	@Test(timeout = 155000L)
	@Ignore
	public void testMultiHostCluster(TestContext context) {
		if (ServiceTest.isCI()) {
			return;
		}

		setLoggingLevel(Level.ERROR);
		startTwoUnitCluster(context);

		// Connect and assert the game ended
		UnityClient client = new UnityClient(context);
		client.createUserAccount(null);
		client.matchmakeAndPlayAgainstAI(null);
		client.waitUntilDone();
		context.assertTrue(client.isGameOver());
	}

	@Test(timeout = 200000L)
	@Ignore
	public void testMultiHostMultiClientCluster(TestContext context) throws InterruptedException {
		if (ServiceTest.isCI()) {
			context.async().complete();
			return;
		}

		setLoggingLevel(Level.ERROR);
		startTwoUnitCluster(context);

		final int count = (Runtime.getRuntime().availableProcessors() / 2 + 1) * 2;
		CountDownLatch latch = new CountDownLatch(count);

		Stream.generate(() -> new Thread(() -> {
			UnityClient client = new UnityClient(context);
			client.createUserAccount(null);
			client.matchmakeAndPlay(null);
			client.waitUntilDone();
			context.assertTrue(client.isGameOver());
			latch.countDown();
		})).limit(count).forEach(Thread::start);

		// Random games can take quite a long time to finish so be patient...
		latch.await(2 * timeoutMillis, TimeUnit.MILLISECONDS);
		context.assertEquals(0L, latch.getCount());
	}

	private void startTwoUnitCluster(TestContext context) {
		List<Future> clusterVerticiesStarts = new ArrayList<>();
		Async async = context.async(3);
		final Handler<AsyncResult<CompositeFuture>> handler = asyncAssertSuccess(context, async);
		for (int i = 0; i < 2; i++) {
			final Future<CompositeFuture> future = Future.future();
			clusterVerticiesStarts.add(future);
			ClusterManager clusterManager = new HazelcastClusterManager(hazelcastInstances.get(i));

			VertxOptions options = new VertxOptions()
					.setClusterManager(clusterManager)
					.setBlockedThreadCheckInterval(blockedThreadCheckInterval);

			Vertx.clusteredVertx(options, asyncAssertSuccess(context, async, result -> {
				verticies.add(result);
				result.exceptionHandler(context.exceptionHandler());
				Spellsource.spellsource().deployAll(result, future);
			}));
		}

		CompositeFuture.all(clusterVerticiesStarts).setHandler(handler);

		// Wait to deploy the clustered vertices with all services
		async.awaitSuccess(timeoutMillis * 2);
	}

	@Before
	public void startServices() throws Exception {
		if (ServiceTest.isCI()) {
			return;
		}

		// From http://vertx.io/docs/vertx-hazelcast/java/
		for (int i = 0; i < 2; i++) {
			hazelcastInstances.add(Hazelcast.newHazelcastInstance(Cluster.getConfig(5701 + i)));
		}

		Mongo.mongo().startEmbedded();
		System.getProperties().put("mongo.url", "mongodb://localhost:27017");
	}

	@After
	public void stopServices(TestContext context) throws IOException {
		Async strict = context.strictAsync(2);
		if (ServiceTest.isCI()) {
			return;
		}

		CompositeFuture.join(verticies.stream().map(v -> {
			Future<Void> future = Future.future();
			if (v != null) {
				v.close(future);
			} else {
				future.complete();
			}
			return future;
		}).collect(Collectors.toList())).setHandler(context.asyncAssertSuccess(then -> {
			hazelcastInstances.forEach(HazelcastInstance::shutdown);
			hazelcastInstances.clear();
			verticies.clear();
			Mongo.mongo().stopEmbedded();
			Mongo.mongo().close();
			Spellsource.spellsource().close();
			System.getProperties().remove("mongo.url");
			strict.complete();
		}));
	}

	@Suspendable
	static public <T> Handler<AsyncResult<T>> asyncAssertSuccess(TestContext context, Async async) {
		return asyncAssertSuccess(context, async, (T ignored) -> {
		});
	}

	@Suspendable
	static public <T> Handler<AsyncResult<T>> asyncAssertSuccess(TestContext context, Async async, Handler<T> resultHandler) {
		return ar -> {
			if (ar.succeeded()) {
				T result = ar.result();
				try {
					resultHandler.handle(result);
					async.countDown();
				} catch (Throwable e) {
					context.fail(e);
				}
			} else {
				context.fail(ar.cause());
			}
		};
	}
}
