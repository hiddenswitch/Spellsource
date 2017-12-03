package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.Spellsource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;

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
		mongo().connectWithEnvironment(vertx);
		Spellsource.spellsource().migrate(vertx, then -> {
			if (then.succeeded()) {
				Spellsource.spellsource().deployAll(vertx, Future.future());
			} else {
				System.err.println("Failed to migrate, deployment aborted.");
			}
		});
	}
}

