package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.core.CompositeFuture.all;

public class Application {
	private static Logger LOGGER = LoggerFactory.getLogger(Application.class);

	protected ServerConfiguration getConfiguration() {
		return Environment.cachedConfigurationOrGet();
	}

	public Future<Void> deploy() {
		return deploy(Environment.vertx());
	}

	protected Future<Void> deploy(Vertx vertx) {
		var deploymentPromise = Promise.<Void>promise();
		vertx.runOnContext(v -> {
			var configuration = getConfiguration();
			var broadcaster = configuration.getApplication().getUseBroadcaster() ? vertx.deployVerticle(Broadcaster.class, new DeploymentOptions().setInstances(1)) : Future.succeededFuture("");
			Environment
					.migrate(configuration)
					.compose(v1 ->
							all(vertx.deployVerticle(Gateway.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
									vertx.deployVerticle(Matchmaking.class, new DeploymentOptions().setInstances(1)),
									vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
									broadcaster)
					)
					.onFailure(t -> LOGGER.error("main: failed with", t))
					.onSuccess(s -> LOGGER.info("main: Started application, now broadcasting"))
					.map((Void) null)
					.onComplete(deploymentPromise);
		});

		return deploymentPromise.future();
	}
}
