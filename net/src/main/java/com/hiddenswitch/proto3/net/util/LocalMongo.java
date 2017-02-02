package com.hiddenswitch.proto3.net.util;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.json.JsonObject;

/**
 * Created by bberman on 2/2/17.
 */
public class LocalMongo {
	/**
	 * please store Starter or RuntimeConfig in a static final field
	 * if you want to use artifact store caching (or else disable caching)
	 */
	private static final MongodStarter starter = MongodStarter.getDefaultInstance();

	private MongodExecutable mongodExecutable;
	private MongodProcess mongodProcess;

	public JsonObject getConfig() {
		return new JsonObject()
				.put("host", "localhost")
				.put("port", 27017)
				.put("db_name", "minionate");
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
