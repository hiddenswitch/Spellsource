package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.rpc.Hiddenswitch.ServerConfiguration;
import io.vertx.core.*;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.core.CompositeFuture.all;

public class Application {
	private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

	protected ServerConfiguration getConfiguration() {
		return Environment.getConfiguration();
	}

	public Future<Vertx> deploy() {
		var vertx = getVertx();
		return vertx.compose(this::deploy).map(v -> vertx.result());
	}

	protected Future<Vertx> getVertx() {
		var options = new VertxOptions(Environment.vertxOptions());
		if (options.getClusterManager() != null) {
			return Vertx.clusteredVertx(options);
		} else {
			return Future.succeededFuture(Vertx.vertx(options));
		}
	}

	protected Future<Vertx> deploy(Vertx vertx) {
		var deploymentPromise = Promise.<Void>promise();
		var configuration = getConfiguration();
		LOGGER.debug("configuration: " + configuration.toString());
		vertx.runOnContext(v -> {
			var broadcaster = configuration.getApplication().getUseBroadcaster() ? vertx.deployVerticle(Broadcaster.class, new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.VIRTUAL_THREAD)) : Future.succeededFuture("");
			// liveness should be going before we start the migration
			Diagnostics.tracing(vertx)
					.compose(w -> Diagnostics.routes(vertx))
					.compose(w -> {
						if (configuration.hasMigration() && configuration.getMigration().getShouldMigrate()) {
							return Environment.migrate(configuration);
						}
						return Future.succeededFuture();
					})
					.compose(w ->
							Future.all(vertx.deployVerticle(Gateway::new, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(Math.max(CpuCoreSensor.availableProcessors() * 2, 8))),
									vertx.deployVerticle(Matchmaking.class, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(1)),
									vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(CpuCoreSensor.availableProcessors() * 2)),
									broadcaster)
					)
					.onFailure(t -> LOGGER.error("main: failed with", t))
					.onSuccess(s -> {
						// TODO: configurable grpc port
						var host = Environment.getHostIpAddress();
						LOGGER.info("Gateway listening grpc (h2c / http2) on " + host + ":" + configuration.getGrpcConfiguration().getPort());
						var metricsPort = configuration.getMetrics().getPort();
						LOGGER.info("Metrics listening on " + host + ":" + metricsPort + configuration.getMetrics().getMetricsRoute());
						LOGGER.info("Liveness listening on " + host + ":" + metricsPort + configuration.getMetrics().getLivenessRoute());
						LOGGER.info("Readiness listening on " + host + ":" + metricsPort + configuration.getMetrics().getReadinessRoute());
					})
					.onSuccess(s -> LOGGER.info("Started application, now broadcasting"))
					.onFailure(t -> LOGGER.error("Failed to deploy", t))
					.map((Void) null)
					.onComplete(deploymentPromise);
		});

		return deploymentPromise.future().map(v -> vertx);
	}
}
