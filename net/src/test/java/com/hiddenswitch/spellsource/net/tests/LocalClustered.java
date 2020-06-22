package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.containers.MongoDBContainer;
import com.hiddenswitch.spellsource.net.Broadcaster;
import com.hiddenswitch.spellsource.net.applications.Applications;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.impl.VertxInternal;
import org.testcontainers.containers.BindMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * The main entry point of a local game server.
 * <p>
 * Starts a local services cluster, tries to migrate and broadcasts over UDP to Unity3D-based clients the IP address of
 * this server once it's ready to be connected to.
 */
public class LocalClustered {
	public static void main(String[] args) throws IOException {
		var rootProjectDir = System.getProperties().getProperty("rootProject.projectDir", "./");

		var dbPath = Path.of(rootProjectDir, ".mongo").toAbsolutePath();
		Files.createDirectories(dbPath);
		var database = new MongoDBContainer("mongo:3.6")
				.withStartupTimeout(Duration.of(20, ChronoUnit.SECONDS));
		database.addFileSystemBind(dbPath.toString(), "/data/db", BindMode.READ_WRITE);
		database.start();
		var mongoUrl = database.getReplicaSetUrl().replace("/test", "/metastone");
		System.getProperties().put("mongo.url", mongoUrl);

		Applications.LOGGER.info("main: mongo.url is {}", mongoUrl);

		GlobalTracer.registerIfAbsent(NoopTracerFactory.create());
		Applications.startServer(vertx -> {
			if (vertx.failed()) {
				database.close();
				return;
			}

			((VertxInternal) vertx.result()).addCloseHook(v -> {
				database.close();
			});

			vertx.result().deployVerticle(Broadcaster.create(), done -> Applications.LOGGER.info("main: Broadcaster deployed. You may now run the game."));
		});
	}
}
