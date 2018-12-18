package com.hiddenswitch.spellsource.applications;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.nio.Address;
import com.hiddenswitch.spellsource.Cluster;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The main entry point of the game server.
 * <p>
 * Starts a clustered service, then tries to migrate the database.
 */
public class Clustered {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("org.mongodb.async.type", "netty");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		LoggerFactory.initialise();
		int vertxClusterPort = Integer.parseInt(System.getenv().getOrDefault("VERTX_CLUSTER_PORT", "5710"));

		// Set significantly longer timeouts
		long nanos = Duration.of(10, ChronoUnit.SECONDS).toNanos();
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getDiscoverySPIConfig("us-west-2"));
		ClusterManager clusterManager = new HazelcastClusterManager(instance);
		String hostAddress = Gateway.getHostAddress();
		Logging.root().info("main: Starting a new Spellsource instance on host {}", hostAddress);
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setClusterHost(hostAddress)
				.setClusterPort(vertxClusterPort)
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
				.setInternalBlockingPoolSize(Runtime.getRuntime().availableProcessors() * 400)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors())
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 400), then -> {

			final Vertx vertx = then.result();

			Mongo.mongo().connectWithEnvironment(vertx);
			Spellsource.spellsource().migrate(vertx, v1 -> {
				if (v1.failed()) {
					Logging.root().error("main: Migration failed: ", v1.cause());
				} else {
					Spellsource.spellsource().deployAll(vertx, v2 -> {
						if (v2.failed()) {
							Logging.root().error("main: Deployment failed: {}", v2.cause().getMessage(), v2.cause());
							System.exit(1);
							return;
						}

						vertx.setTimer(20000L, v3 -> {
							List<String> hazelcastMembers = com.hiddenswitch.spellsource.util.Hazelcast.getHazelcastInstance().getCluster().getMembers().stream().map(Member::getAddress).map(Address::toString).collect(toList());
							List<String> vertxMembers = com.hiddenswitch.spellsource.util.Hazelcast.getClusterManager().getNodes();
							Logging.root().info("main: Cluster connected to {} hosts in Vertx, {} in Hazelcast", vertxMembers, hazelcastMembers);
						});
					});
				}
			});
		});
	}
}

