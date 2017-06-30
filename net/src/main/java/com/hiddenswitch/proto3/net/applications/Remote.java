package com.hiddenswitch.proto3.net.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.minionate.Minionate;
import com.hiddenswitch.proto3.net.util.Mongo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Remote {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("javax.net.ssl.trustStore", "/home/ubuntu/Minionate/metastone/mongostore");
		System.setProperty("javax.net.ssl.trustStorePassword", "ilikeamiga");
		System.setProperty("org.mongodb.async.type", "netty");

		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);

		// Set significantly longer timeouts
		long nanos = Duration.of(4, ChronoUnit.MINUTES).toNanos();
		Vertx vertx = Vertx.vertx(new VertxOptions()
				.setBlockedThreadCheckInterval(Duration.of(8, ChronoUnit.SECONDS).toMillis())
				.setWarningExceptionTime(nanos)
				.setMaxEventLoopExecuteTime(nanos)
				.setMaxWorkerExecuteTime(nanos));

		Mongo.mongo().connect(vertx, "mongodb://spellsource1:9AD3uubaeIf71a4M11lPVAV2mJcbPzV1EC38Y4WF26M@aws-us-east-1-portal.9.dblayer.com:20276/production?ssl=true");
		Minionate.minionate().migrate(vertx, then -> {
			if (then.failed()) {
				root.error("Migration failed: " + then.cause().getMessage());
			}
			Minionate.minionate().deployAll(vertx, Future.future());
		});
	}

}
