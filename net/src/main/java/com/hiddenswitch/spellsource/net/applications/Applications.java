package com.hiddenswitch.spellsource.net.applications;

import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.launcher.commands.BareCommand;
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
		var clusterManagerPort = Configuration.clusterManagerPort();
		var vertxClusterPort = Configuration.vertxClusterPort();
		io.vertx.core.logging.LoggerFactory.initialise();
		var nanos = Duration.of(8000, ChronoUnit.MILLIS).toNanos();
		var nodes = System.getenv().getOrDefault("BOOTSTRAP_ADDRESS", "localhost:" + clusterManagerPort);
		var clusterManagerFut = Cluster.create(clusterManagerPort, nodes);

		clusterManagerFut
				.compose(clusterManager -> {
					var promise = Promise.<Vertx>promise();
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
													.setEmbeddedServerEndpoint("/metrics"))
											.setEnabled(true))
							.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors()), promise);
					return promise.future();
				})
				.compose(vertx -> {
					// Set up tracing now that we started vertx
					Tracing.initializeGlobal(vertx);
					// Set up metrics
					Metrics.defaultMetrics();
					var promise = Promise.<Void>promise();
					Migrations.migrate(vertx, promise);
					return promise.future().map(ignored -> vertx);
				})
				.compose(vertx -> {
					var promise = Promise.<CompositeFuture>promise();
					Spellsource.spellsource().deployAll(vertx, promise);
					return promise.future().map(ignored -> vertx);
				})
				.compose(vertx -> {
					var hook = new Thread(BareCommand.getTerminationRunnable(vertx, io.vertx.core.logging.LoggerFactory.getLogger(Applications.class), null));
					hook.setName("vertx-shutdown-hook");
					Runtime.getRuntime().addShutdownHook(hook);
					return Future.succeededFuture(vertx);
				})
				.onComplete(handler);
	}
}
