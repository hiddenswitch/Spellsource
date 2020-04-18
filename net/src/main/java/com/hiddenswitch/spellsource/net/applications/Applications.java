package com.hiddenswitch.spellsource.net.applications;

import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.RpcClient;
import io.atomix.cluster.Node;
import io.atomix.core.Atomix;
import io.atomix.utils.net.Address;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		io.vertx.core.logging.LoggerFactory.initialise();
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
			Migrations.migrate(vertx, v1 -> {
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
