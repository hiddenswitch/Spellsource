package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.Cluster;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.Tracing;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
		int atomixPort = Integer.parseInt(System.getenv().getOrDefault("ATOMIX_PORT", "5701"));
		int vertxClusterPort = Integer.parseInt(System.getenv().getOrDefault("VERTX_CLUSTER_PORT", "5710"));

		// Set significantly longer timeouts
		long nanos = Duration.of(10, ChronoUnit.SECONDS).toNanos();

		String hostAddress = Gateway.getHostAddress();
		if (hostAddress == null) {
			throw new UnsupportedOperationException();
		}

		Atomix atomix = Cluster.create(atomixPort);
		atomix.start().join();
		ClusterManager clusterManager = new AtomixClusterManager(atomix);
		Logging.root().info("main: Starting a new Spellsource instance on host {}", hostAddress);
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setEventBusOptions(new EventBusOptions().setPort(vertxClusterPort))
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
//				.setMetricsOptions(getMetrics())
				.setInternalBlockingPoolSize(Runtime.getRuntime().availableProcessors() * 400)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors())
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 400), then -> {
			final Vertx vertx = then.result();
			Tracing.initializeGlobal(vertx);

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
					});
				}
			});
		});
	}

	public static DropwizardMetricsOptions getMetrics() {
		return new DropwizardMetricsOptions().setEnabled(true)
				.addMonitoredEventBusHandler(new Match().setValue("\\w+/.*").setType(MatchType.REGEX));
	}
}

