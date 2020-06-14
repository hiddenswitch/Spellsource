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
		var path = Path.of(rootProjectDir, ".mongo").toAbsolutePath();
		Files.createDirectories(path);
		var container = new MongoDBContainer("mongo:3.6")
				.withStartupTimeout(Duration.of(20, ChronoUnit.SECONDS));
		container.addFileSystemBind(path.toString(), "/data/db", BindMode.READ_WRITE);
		container.start();
		String mongoUrl = container.getReplicaSetUrl().replace("/test", "/metastone");
		System.out.println(mongoUrl);
		System.getProperties().put("mongo.url", mongoUrl);
		GlobalTracer.registerIfAbsent(NoopTracerFactory.create());
		Applications.startServer(vertx -> vertx.result().deployVerticle(Broadcaster.create(), done -> {
			((VertxInternal) vertx.result()).addCloseHook(v -> container.close());
			Applications.LOGGER.info("main: Broadcaster deployed");
		}));
	}
}
