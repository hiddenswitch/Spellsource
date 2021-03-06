package com.hiddenswitch.framework;

import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import com.hiddenswitch.protos.Serialization;
import io.vertx.core.*;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.tracing.opentracing.OpenTracingOptions;
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
		// related to configuring the cluster manager
		System.getProperties().put("java.net.preferIPv4Stack", "true");
		System.getProperties().put("jgroups.bind.address", "GLOBAL");
		System.getProperties().put("jgroups.bind.port", "7800");
		var isKubernetes = System.getenv().containsKey("KUBERNETES_SERVICE_HOST");
		String config;
		if (isKubernetes) {
			config = "default-configs/default-jgroups-kubernetes.xml";
		} else {
			config = "default-configs/default-jgroups-udp.xml";
		}
		System.getProperties().put("vertx.jgroups.config", config);
		return Vertx.clusteredVertx(new VertxOptions(Environment.vertxOptions())
				// todo: https://github.com/eclipse-vertx/vert.x/issues/3829 awaiting fix
				.setTracingOptions(new OpenTracingOptions(Tracing.tracing()))
				.setClusterManager(new InfinispanClusterManager()));
	}

	protected Future<Vertx> deploy(Vertx vertx) {
		var deploymentPromise = Promise.<Void>promise();
		var configuration = getConfiguration();
		LOGGER.debug("main: Configuration: " + configuration.toString());
		vertx.runOnContext(v -> {
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
					.onFailure(t -> LOGGER.error("main: Failed to deploy",t))
					.map((Void) null)
					.onComplete(deploymentPromise);
		});

		return deploymentPromise.future().map(v -> vertx);
	}
}
