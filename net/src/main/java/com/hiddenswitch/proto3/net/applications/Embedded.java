package com.hiddenswitch.proto3.net.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.net.impl.ServerImpl;
import io.vertx.core.Vertx;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by bberman on 11/29/16.
 */
public class Embedded {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);

		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl());
	}
}
