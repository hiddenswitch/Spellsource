package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import io.vertx.core.*;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.core.CompositeFuture.all;

public class Application {
	private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

	protected ServerConfiguration getConfiguration() {
		return Environment.getConfiguration();
	}

	public Future<Void> deploy() {
		return getVertx().compose(this::deploy);
	}

	protected Future<Vertx> getVertx() {
		// related to configuring the cluster manager
		System.getProperties().put("java.net.preferIPv4Stack", "true");
		// we never want to change this for a clustered vertx, since a clustered vertx will always run inside minikube
		System.getProperties().put("vertx.jgroups.config", "default-configs/default-jgroups-kubernetes.xml");
		return Vertx.clusteredVertx(new VertxOptions(Environment.vertxOptions())
				.setClusterManager(new InfinispanClusterManager()));
	}

	protected Future<Void> deploy(Vertx vertx) {
		var deploymentPromise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var configuration = getConfiguration();
			var broadcaster = configuration.getApplication().getUseBroadcaster() ? vertx.deployVerticle(Broadcaster.class, new DeploymentOptions().setInstances(1)) : Future.succeededFuture("");
			// liveness should be going before we start the migration
			Diagnostics.tracing(vertx)
					.compose(v1 -> Diagnostics.routes(vertx))
					.compose(v1 -> {
						if (configuration.hasMigration() && configuration.getMigration().getShouldMigrate()) {
							return Environment.migrate(configuration);
						}
						return Future.succeededFuture();
					})
					.compose(v1 ->
							all(vertx.deployVerticle(Gateway.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
									vertx.deployVerticle(Matchmaking.class, new DeploymentOptions().setInstances(1)),
									vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
									broadcaster)
					)
					.onFailure(t -> LOGGER.error("main: failed with", t))
					.onSuccess(s -> {
						// TODO: configurable grpc port
						var host = Environment.getHostIpAddress();
						LOGGER.info("main: Gateway listening grpc (h2c / http2) on " + host + ":" + configuration.getGrpcConfiguration().getPort());
						LOGGER.info("main: Metrics listening on " + host + ":" + configuration.getMetrics().getPort() + configuration.getMetrics().getMetricsRoute());
						LOGGER.info("main: Liveness listening on " + host + ":" + configuration.getMetrics().getPort() + configuration.getMetrics().getLivenessRoute());
						LOGGER.info("main: Readiness listening on " + host + ":" + configuration.getMetrics().getPort() + configuration.getMetrics().getReadinessRoute());
					})
					.onSuccess(s -> LOGGER.info("main: Started application, now broadcasting"))
					.map((Void) null)
					.onComplete(deploymentPromise);
		});

		return deploymentPromise.future();
	}
}
