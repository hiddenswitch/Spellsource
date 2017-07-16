package com.hiddenswitch.spellsource.util;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.Downloader;
import de.flapdoodle.embed.process.store.ExtractedArtifactStoreBuilder;
import io.vertx.core.json.JsonObject;

/**
 * Created by bberman on 2/2/17.
 */
public class LocalMongo {
	/**
	 * please store Starter or RuntimeConfig in a static final field
	 * if you want to use artifact store caching (or else disable caching)
	 */
	private static final MongodStarter starter;
	private static final Command command;
	private static final ExtractedArtifactStoreBuilder artifactStoreBuilder;
	private MongodExecutable mongodExecutable;
	private MongodProcess mongodProcess;

	static {

		final FixedPath fixedPath = new FixedPath(System.getProperty("user.dir") + "/.mongo");
		IDirectory artifactStorePath = fixedPath;

		command = Command.MongoD;
		artifactStoreBuilder = new ExtractedArtifactStoreBuilder()
				.extractDir(fixedPath)
				.tempDir(fixedPath)
				.extractExecutableNaming(new UserTempNaming())
				.executableNaming(new UserTempNaming())
				.downloader(new Downloader())
				.download(new DownloadConfigBuilder()
						.defaultsForCommand(command)
						.artifactStorePath(artifactStorePath)
						.build());

		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
				.defaults(command)
				.artifactStore(artifactStoreBuilder)
				.build();

		starter = MongodStarter.getInstance(runtimeConfig);
	}

	public void start() throws Exception {
		mongodExecutable = starter.prepare(new MongodConfigBuilder()
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
