package com.hiddenswitch.proto3.net.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.minionate.Minionate;
import com.hiddenswitch.proto3.net.util.Mongo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class Remote {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);

		Vertx vertx = Vertx.vertx();
		System.getProperties().setProperty("javax.net.ssl.trustStore", "/home/ubuntu/Minionate/metastone/mongostore");
		System.getProperties().setProperty("javax.net.ssl.trustStorePassword", "ilikeamiga");
		Mongo.mongo().connect(vertx, "mongodb://spellsource1:9AD3uubaeIf71a4M11lPVAV2mJcbPzV1EC38Y4WF26M@aws-us-east-1-portal.9.dblayer.com:20276/production?ssl=true");
		Minionate.minionate().deployAll(vertx, Future.future());
	}
}
