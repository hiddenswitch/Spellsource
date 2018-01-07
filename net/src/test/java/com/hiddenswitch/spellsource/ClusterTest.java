package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.config.*;
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
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(VertxUnitRunner.class)
public class ClusterTest {
	private Logger logger = LoggerFactory.getLogger(ClusterTest.class);
	private List<Vertx> verticies = new ArrayList<>();
	private List<HazelcastInstance> hazelcastInstances = new ArrayList<>();

	public void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testClusteredDeploy(TestContext context) {
		// For debug purposes
		final int blockedThreadCheckInterval = (int) Duration.of(8, ChronoUnit.SECONDS).toMillis();
		final long timeoutMillis = Duration.of(150, ChronoUnit.SECONDS).toMillis();

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

	@Before
	public void startServices() throws Exception {
		verticies.clear();
		final Properties properties = new Properties();
		properties.setProperty("hazelcast.shutdownhook.enabled", "false");
		properties.setProperty("hazelcast.logging.type", "slf4j");
		// From http://vertx.io/docs/vertx-hazelcast/java/
		for (int i = 0; i < 2; i++) {
			hazelcastInstances.add(Hazelcast.newHazelcastInstance(new Config()
					.setClassLoader(getClass().getClassLoader())
					.setNetworkConfig(new NetworkConfig()
							.setPort(5701 + i)
							.setJoin(new JoinConfig()
									.setMulticastConfig(new MulticastConfig()
											.setEnabled(false))
									.setTcpIpConfig(new TcpIpConfig()
											.setEnabled(true)
											.setMembers(Arrays.asList("localhost:5701", "localhost:5702")))))
					.setProperties(properties)
					.addMultiMapConfig(new MultiMapConfig()
							.setBackupCount(1)
							.setName("__vertx.subs"))
					.addMapConfig(new MapConfig()
							.setName("__vertx.haInfo")
							.setTimeToLiveSeconds(0)
							.setMaxIdleSeconds(0)
							.setEvictionPolicy(EvictionPolicy.NONE)
							.setMaxSizeConfig(new MaxSizeConfig().setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.PER_NODE).setSize(0))
							.setEvictionPercentage(25)
							.setMergePolicy("com.hazelcast.map.merge.LatestUpdateMapMergePolicy"))
					.addSemaphoreConfig(new SemaphoreConfig()
							.setName("__vertx.*")
							.setInitialPermits(1))));
		}


		Mongo.mongo().startEmbedded();
		System.getProperties().put("mongo.url", "mongodb://localhost:27017");
	}

	@After
	public void stopServices(TestContext context) throws IOException {
		CompositeFuture.join(verticies.stream().map(v -> {
			Future<Void> future = Future.future();
			v.close(future);
			return future;
		}).collect(Collectors.toList())).setHandler(context.asyncAssertSuccess(then -> {
			hazelcastInstances.forEach(HazelcastInstance::shutdown);
			Mongo.mongo().stopEmbedded();
			System.getProperties().remove("mongo.url");
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
