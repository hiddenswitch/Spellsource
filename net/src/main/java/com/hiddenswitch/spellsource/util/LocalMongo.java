package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by bberman on 2/2/17.
 */
public class LocalMongo {
	/**
	 * please store Starter or RuntimeConfig in a static final field if you want to use artifact store caching (or else
	 * disable caching)
	 */
	private static final MongodStarter starter;
	private static final Command command;
	private static final ExtractedArtifactStoreBuilder artifactStoreBuilder;
	private static final Storage replication;
	private MongodExecutable mongodExecutable;
	private MongodProcess mongodProcess;

	static {
		final String path = System.getProperty("user.dir") + "/.mongo";
		final FixedPath fixedPath = new FixedPath(path);
		replication = new Storage(path + "/db", null, 0);
		System.out.println("An embedded mongod was successfully started.");
		System.out.println("Path to database: " + replication.getDatabaseDir());
		command = Command.MongoD;
		artifactStoreBuilder = new ExtractedArtifactStoreBuilder()
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

		starter = MongodStarter.getInstance(runtimeConfig);
	}

	public void start() throws Exception {
		mongodExecutable = starter.prepare(new MongodConfigBuilder()
				.replication(replication)
				.version(Version.Main.PRODUCTION)
				.net(new Net("localhost", 27017, Network.localhostIsIPv6()))
				.build());
		mongodProcess = mongodExecutable.start();
	}

	public void stop() throws Exception {
		mongodProcess.stop();
		mongodExecutable.stop();
	}
}
