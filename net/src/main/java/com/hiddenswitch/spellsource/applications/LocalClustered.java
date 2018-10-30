package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.Broadcaster;
import com.hiddenswitch.spellsource.Cluster;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * The main entry point of a local game server.
 * <p>
 * Starts a local services cluster, tries to migrate and broadcasts over UDP to Unity3D-based clients the IP address of
 * this server once it's ready to be connected to.
 */
public class LocalClustered {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("org.mongodb.async.type", "netty");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		LoggerFactory.initialise();

		// Set significantly longer timeouts
		long nanos = Duration.of(4, ChronoUnit.MINUTES).toNanos();
		final HazelcastInstance instance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5701));
		ClusterManager clusterManager = new HazelcastClusterManager(instance);
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setClusterHost(Gateway.getHostAddress())
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
				.setInternalBlockingPoolSize(Runtime.getRuntime().availableProcessors() * 40)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors())
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 40), then -> {

			final Vertx vertx = then.result();

			Mongo.mongo().connectWithEnvironment(vertx);
			Spellsource.spellsource().migrate(vertx, then2 -> {
				if (then2.failed()) {
					Logging.root().error("Migration failed", then2.cause());
				} else {
					Spellsource.spellsource().deployAll(vertx, andThen -> {
						vertx.deployVerticle(Broadcaster.create(), Future.future());
						System.out.println("***** SERVER IS READY. START THE CLIENT. *****");
					});
				}
			});
		});
	}
}
