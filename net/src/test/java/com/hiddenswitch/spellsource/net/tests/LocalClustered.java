package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.containers.*;
import com.hiddenswitch.spellsource.net.Broadcaster;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.applications.Applications;
import io.vertx.core.impl.VertxInternal;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * The main entry point of a local game server.
 * <p>
 * Starts a local services cluster, tries to migrate and broadcasts over UDP to Unity3D-based clients the IP address of
 * this server once it's ready to be connected to.
 */
public class LocalClustered {
	public static void main(String[] args) throws IOException {
		var rootProjectDir = System.getProperties().getProperty("rootProject.projectDir", "./");

		// Expose ports to containers
		Testcontainers.exposeHostPorts(Configuration.apiGatewayPort(), Configuration.metricsPort());

		var dbPath = Path.of(rootProjectDir, ".mongo").toAbsolutePath();
		Files.createDirectories(dbPath);
		var database = new MongoDBContainer()
				.withStartupTimeout(Duration.of(20, ChronoUnit.SECONDS));
		database.addFileSystemBind(dbPath.toString(), "/data/db", BindMode.READ_WRITE);

		var jaeger = new JaegerContainer();
		var prometheus = new PrometheusContainer();
		var grafana = new GrafanaContainer(jaeger.getAgentHost(), jaeger.getAgentPort())
				.withFileSystemBind(Path.of(rootProjectDir, "grafana", "data").toString(), GrafanaContainer.GRAFANA_STORAGE_DIR);
		var redis = new RedisContainer();

		var closeables = Stream.of(database, jaeger, prometheus, grafana, redis)
				.parallel()
				.peek(GenericContainer::start)
				.map(container -> (AutoCloseable) container)
				.toArray(AutoCloseable[]::new);

		var mongoUrl = database.getReplicaSetUrl().replace("/test", "/metastone");
		// Passes the database URL to the Mongo code
		System.getProperties().put("mongo.url", mongoUrl);
		System.getProperties().put("redis.url", redis.getRedisUrl());
		// Passes jaeger information to the Tracing code
		System.getProperties().put("JAEGER_AGENT_HOST", jaeger.getAgentHost());
		System.getProperties().put("JAEGER_AGENT_PORT", jaeger.getAgentPort().toString());


		Applications.startServer(vertx -> {
			if (vertx.failed()) {
				for (var closeable : closeables) {
					try {
						closeable.close();
					} catch (Exception ignored) {
					}
				}
				return;
			}

			((VertxInternal) vertx.result()).addCloseHook(v -> {
				for (var closeable : closeables) {
					try {
						closeable.close();
					} catch (Exception ignored) {
					}
				}
			});

			Applications.LOGGER.info("main: mongo.url is {}", mongoUrl);
			Applications.LOGGER.info("main: jaeger.frontend.url is {}", jaeger.getFrontendUrl());
			Applications.LOGGER.info("main: prometheus.url is {}", prometheus.getPrometheusUrl());
			Applications.LOGGER.info("main: grafana.frontend.url is {}", grafana.getGrafanaUrl());
			vertx.result().deployVerticle(Broadcaster.create(), done -> Applications.LOGGER.info("main: Broadcaster deployed. You may now run the game."));
		});
	}
}
