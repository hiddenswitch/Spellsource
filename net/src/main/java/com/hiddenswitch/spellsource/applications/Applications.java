package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.Cluster;
import com.hiddenswitch.spellsource.Configuration;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.Tracing;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.utils.net.Address;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface Applications {
	Logger LOGGER = LoggerFactory.getLogger(Applications.class);

	static void startServer(Handler<AsyncResult<Vertx>> handler) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("org.mongodb.async.type", "netty");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		int atomixPort = Configuration.atomixPort();
		int vertxClusterPort = Configuration.vertxClusterPort();
		LoggerFactory.initialise();
		long nanos = Duration.of(RpcClient.DEFAULT_TIMEOUT, ChronoUnit.MILLIS).toNanos();
		Node[] nodes = new Node[0];
		if (Configuration.atomixBootstrapNode() != null) {
			nodes = new Node[]{Node.builder()
					.withAddress(Address.from(Configuration.atomixBootstrapNode()))
					.build()};
		}

		Atomix atomix = Cluster.create(atomixPort, nodes);
		atomix.start().join();
		ClusterManager clusterManager = new AtomixClusterManager(atomix);
		Vertx.clusteredVertx(new VertxOptions()
				.setClusterManager(clusterManager)
				.setEventBusOptions(new EventBusOptions().setPort(vertxClusterPort))
				.setPreferNativeTransport(true)
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors()), then -> {

			Vertx vertx = then.result();
			Tracing.initializeGlobal(vertx);
			Spellsource.spellsource().migrate(vertx, v1 -> {
				if (v1.failed()) {
					LOGGER.error("main: Migration failed", v1.cause());
					handler.handle(Future.failedFuture(v1.cause()));
					return;
				}
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
