package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Remote {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("javax.net.ssl.trustStore", "./mongostore");
		System.setProperty("javax.net.ssl.trustStorePassword", "ilikeamiga");
		System.setProperty("org.mongodb.async.type", "netty");

		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);

		// Set significantly longer timeouts
		long nanos = Duration.of(4, ChronoUnit.MINUTES).toNanos();
		Vertx vertx = Vertx.vertx(new VertxOptions()
				.setBlockedThreadCheckInterval(RpcClient.DEFAULT_TIMEOUT)
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos)
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors() * 10));

		Mongo.mongo().connect(vertx, "mongodb://spellsource1:9AD3uubaeIf71a4M11lPVAV2mJcbPzV1EC38Y4WF26M@aws-us-east-1-portal.9.dblayer.com:20276/production?ssl=true");
		Spellsource.Spellsource().migrate(vertx, then -> {
			if (then.failed()) {
				root.error("Migration failed: " + then.cause().getMessage());
			}
			Spellsource.Spellsource().deployAll(vertx, Future.future());
		});
	}

}
