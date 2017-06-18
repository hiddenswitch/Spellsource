package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.util.Mongo;
import com.hiddenswitch.proto3.net.util.UnityClient;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(VertxUnitRunner.class)
public class ClusterTest {
	private TestingServer zkTestServer;
	private Logger logger = LoggerFactory.getLogger(ClusterTest.class);
	private List<Vertx> verticies = new ArrayList<>();

	public void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testClusteredDeploy(TestContext context) {

		setLoggingLevel(Level.ERROR);

		ZookeeperClusterManager clusterManager = new ZookeeperClusterManager(new JsonObject()
				.put("zookeeperHosts", "127.0.0.1:2181"));

		VertxOptions options = new VertxOptions().setClusterManager(clusterManager);


		// Test instance. nothing special here
		Vertx vertxTest = Vertx.vertx();
		verticies.add(vertxTest);

		// Instance 1, deploy games, AI and logic
		Vertx.clusteredVertx(options, context.asyncAssertSuccess(vertx1 -> {
			verticies.add(vertx1);
			final List<Future> services = (List<Future>) Stream.of(new GamesImpl(), new BotsImpl(), new LogicImpl())
					.map(service -> {
						Future<String> future = Future.future();
						vertx1.deployVerticle(service, context.asyncAssertSuccess(result -> {
							future.handle(Future.succeededFuture(result));
						}));
						return (Future) future;
					}).collect(Collectors.toList());

			CompositeFuture.all(services).setHandler(context.asyncAssertSuccess());
		}));

		clusterManager = new ZookeeperClusterManager(new JsonObject()
				.put("zookeeperHosts", "127.0.0.1:2181"));

		options = new VertxOptions().setClusterManager(clusterManager);

		// Instance 2, deploy all the supporting services
		Vertx.clusteredVertx(options, context.asyncAssertSuccess(vertx2 -> {
			verticies.add(vertx2);
			final List<Future> gameServices = (List<Future>) Stream.of(new AccountsImpl(), new CardsImpl(), new DecksImpl(), new DraftImpl(), new InventoryImpl(), new MatchmakingImpl(), new GatewayImpl())
					.map(service -> {
						Future<String> future = Future.future();
						vertx2.deployVerticle(service, context.asyncAssertSuccess(result -> {
							future.handle(Future.succeededFuture(result));
						}));
						return (Future) future;
					}).collect(Collectors.toList());

			CompositeFuture.all(gameServices).setHandler(context.asyncAssertSuccess(then2 -> {
				// Create a unity client and try to finish a game.
				vertxTest.executeBlocking(done -> {
					UnityClient client = new UnityClient(context);
					client.createUserAccount(null);
					client.matchmakeAndPlayAgainstAI(null);
					client.waitUntilDone();
					context.assertTrue(client.isGameOver());
					done.complete();
				}, context.asyncAssertSuccess());
			}));
		}));
	}

	@Before
	public void startServices() throws Exception {
		verticies.clear();
		zkTestServer = new TestingServer(2181);
		Mongo.mongo().startEmbedded();
		System.getProperties().put("mongo.url", "mongodb://localhost:27017");
	}

	@After
	public void stopServices() throws IOException {
		for (Vertx vertx : verticies) {
			vertx.close();
		}
		zkTestServer.stop();
		Mongo.mongo().stopEmbedded();
		System.getProperties().remove("mongo.url");
	}
}
