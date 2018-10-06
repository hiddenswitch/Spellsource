package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
import com.mongodb.async.client.MongoClients;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.runtime.Mongod;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.Downloader;
import de.flapdoodle.embed.process.store.ExtractedArtifactStoreBuilder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.SystemUtils;
import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bberman on 2/2/17.
 */
public class LocalMongo {
	public static final boolean MONGO_EMBEDDED_REPLICA_SET = Boolean.getBoolean("MONGO_EMBEDDED_REPLICA_SET");
	private final int randomPort = new Random().nextInt(1000) + 27018;
	private MongodProcess mongodProcess;

	public String getUrl() {
		return "mongodb://localhost:" + Integer.toString(this.getPort()) + "/metastone" + (MONGO_EMBEDDED_REPLICA_SET ? "?replicaSet=localReplSet" : "");
	}

	public void start() throws Throwable {
		final String path = System.getProperty("user.dir") + "/.mongo";
		final FixedPath fixedPath = new FixedPath(path);
		Storage replication = MONGO_EMBEDDED_REPLICA_SET ? new Storage(path + "/db", "localReplSet", 5000)
				: new Storage(path + "/db", null, 0);
		Command command = Command.MongoD;
		ExtractedArtifactStoreBuilder artifactStoreBuilder = new ExtractedArtifactStoreBuilder()
				.extractDir(fixedPath)
				.tempDir(fixedPath)
				.extractExecutableNaming(new UserTempNaming())
				.executableNaming(new UUIDTempNaming())
				.downloader(new Downloader())
				.download(new DownloadConfigBuilder()
						.defaultsForCommand(command)
						.artifactStorePath(fixedPath)
						.build());

		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Mongod.class);
		logger.setLevel(Level.ERROR);

		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
				.defaultsWithLogger(command, logger)
				.artifactStore(artifactStoreBuilder)
				.build();

		CountDownLatch latch = new CountDownLatch(1);
		final MongodConfigBuilder builder = new MongodConfigBuilder()
				.version(MONGO_EMBEDDED_REPLICA_SET ? Version.Main.V3_6 : Version.Main.V3_4)
				.net(new Net("localhost", randomPort, Network.localhostIsIPv6()));
		if (MONGO_EMBEDDED_REPLICA_SET) {
			builder.replication(replication);
		}
		final IMongodConfig config = builder
				.build();
		MongodExecutable mongodExecutable = MongodStarter.getInstance(runtimeConfig)
				.prepare(config);
		mongodProcess = mongodExecutable.start();

		if (MONGO_EMBEDDED_REPLICA_SET) {
			final JsonObject replSetInitiateConfig = new JsonObject();

			replSetInitiateConfig.put("replSetInitiate",
					new JsonObject()
							.put("_id", "localReplSet")
							.put("members", new JsonArray(Collections.singletonList(new JsonObject()
									.put("_id", 0).put("host", "localhost:" + Integer.toString(randomPort))))));

			com.mongodb.async.client.MongoClient client = MongoClients.create("mongodb://localhost:" + Integer.toString(randomPort) + "/");
			AtomicReference<Throwable> err = new AtomicReference<>();
			client.getDatabase("local").getCollection("system.replset")
					.count((res, t1) -> {
						if (t1 != null) {
							err.set(t1);
						}
						if (res == null || res > 0L) {
							latch.countDown();
							client.close();
						} else {
							client.getDatabase("admin").runCommand(
									BsonDocument.parse(replSetInitiateConfig.encode()), (result, t2) -> {
										if (t2 != null) {
											err.set(t2);
										}
										latch.countDown();
										client.close();
									});
						}
					});

			latch.await(30L, TimeUnit.SECONDS);
			if (err.get() != null) {
				throw err.get();
			}
		}
	}

	public void stop() throws Exception {
		if (MONGO_EMBEDDED_REPLICA_SET) {
			CountDownLatch latch = new CountDownLatch(1);
			// Since this is a replica set mongo, force shutdown.
			JsonObject shutdown = new JsonObject()
					.put("shutdown", 1)
					.put("force", true);
			com.mongodb.async.client.MongoClient client = MongoClients.create("mongodb://localhost:" + Integer.toString(randomPort) + "/?replicaSet=localReplSet");
			client.getDatabase("admin").runCommand(BsonDocument.parse(shutdown.encode()), (result, t) -> {
				latch.countDown();
				client.close();
			});
			latch.await(30L, TimeUnit.SECONDS);
		}
		try {
			mongodProcess.stop();
		} catch (RuntimeException ignored) {
		}
	}

	public int getPort() {
		return randomPort;
	}
}
