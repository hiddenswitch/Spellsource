package com.hiddenswitch.spellsource.net.applications;

import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.Cluster;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.Migrations;
import com.hiddenswitch.spellsource.net.Spellsource;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.utils.net.Address;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface Applications {
	Logger LOGGER = LoggerFactory.getLogger(Applications.class);

	/**
	 * Starts the game server.
	 *
	 * @param handler
	 */
	static void startServer(Handler<AsyncResult<Vertx>> handler) {
		// Workaround for IPv6 systems
		System.setProperty("java.net.preferIPv4Stack", "true");
		// Improves mongodb performance by using the async version of the driver
		System.setProperty("org.mongodb.async.type", "netty");
		// Vertx should log using SLF4J a java logging library
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		// Shared data between all instances of the server is done through a library called Atomix
		// We'll start atomix now
		int atomixPort = Configuration.atomixPort();
		int vertxClusterPort = Configuration.vertxClusterPort();
		io.vertx.core.logging.LoggerFactory.initialise();
		long nanos = Duration.of(8000, ChronoUnit.MILLIS).toNanos();
		Node[] nodes = new Node[0];
		if (Configuration.atomixBootstrapNode() != null) {
			nodes = new Node[]{Node.builder()
					.withAddress(Address.from(Configuration.atomixBootstrapNode()))
					.build()};
		}

		Atomix atomix = Cluster.create(atomixPort, nodes);
		atomix.start().join();
		ClusterManager clusterManager = new AtomixClusterManager(atomix);

		// Start the vertx networking, concurrency and HTTP framework
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setEventBusOptions(new EventBusOptions().setPort(vertxClusterPort))
				.setPreferNativeTransport(true)
				.setBlockedThreadCheckInterval(8000L)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
				.setMetricsOptions(
						new MicrometerMetricsOptions()
								.setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true)
										.setStartEmbeddedServer(true)
										.setEmbeddedServerOptions(new HttpServerOptions().setPort(Configuration.metricsPort()))
										.setEmbeddedServerEndpoint("/metrics/vertx"))
								.setEnabled(true))
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors()), then -> {

			Vertx vertx = then.result();
			// Set up tracing now that we started vertx
			Tracing.initializeGlobal(vertx);

			// Migrate the database
			Migrations.migrate(vertx, v1 -> {
				if (v1.failed()) {
					LOGGER.error("main: Migration failed", v1.cause());
					handler.handle(Future.failedFuture(v1.cause()));
					return;
				}

				// Deploy the services that make up the game (services are called verticles inside Vertx_
				Spellsource.spellsource().deployAll(vertx, v2 -> {
					if (v2.failed()) {
						LOGGER.error("main: Deployment failed", v2.cause());
						handler.handle(Future.failedFuture(v2.cause()));
						return;
					}

					LOGGER.info("main: {} clustered with members {}", clusterManager.getNodeID(), clusterManager.getNodes());
					handler.handle(Future.succeededFuture(vertx));
				});
			});
		});
	}
}
