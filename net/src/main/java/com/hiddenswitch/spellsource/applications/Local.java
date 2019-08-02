package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.Broadcaster;
import com.hiddenswitch.spellsource.Gateway;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.Tracing;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Starts a local, unclustered instance of the Spellsource services. Does not create a Hazelcast instance.
 */
public class Local {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("org.mongodb.async.type", "netty");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		LoggerFactory.initialise();

		// Set significantly longer timeouts
		long nanos = Duration.of(10, ChronoUnit.SECONDS).toNanos();

		String hostAddress = Gateway.getHostAddress();
		Logging.root().info("main: Starting a new Spellsource instance on host {}", hostAddress);
		Vertx vertx = Vertx.vertx(new VertxOptions()
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
//				.setMetricsOptions(Clustered.getMetrics())
				.setInternalBlockingPoolSize(Runtime.getRuntime().availableProcessors() * 400)
				.setEventLoopPoolSize(Runtime.getRuntime().availableProcessors())
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 400));
		vertx.runOnContext(v -> {
			Tracing.initializeGlobal(vertx);

			Mongo.mongo().connectWithEnvironment(vertx);
			Spellsource.spellsource().migrate(vertx, v1 -> {
				if (v1.failed()) {
					Logging.root().error("main: Migration failed: ", v1.cause());
				} else {
					Spellsource.spellsource().deployAll(vertx, v2 -> {
						if (v2.failed()) {
							Logging.root().error("main: Deployment failed: {}", v2.cause().getMessage(), v2.cause());
							System.exit(1);
						} else {
							boolean shouldDeployBroadcaster = Boolean.parseBoolean(System.getenv().getOrDefault("SPELLSOURCE_BROADCAST", "true"));
							if (shouldDeployBroadcaster) {
								vertx.deployVerticle(Broadcaster.create(), v3 -> {
									if (v3.succeeded()) {
										System.out.println("***** SERVER IS READY. START THE CLIENT. *****");
									} else {
										Logging.root().error("main: Failed to deploy broadcaster");
										System.exit(1);
									}
								});
							}
						}
					});
				}
			});
		});
	}
}
