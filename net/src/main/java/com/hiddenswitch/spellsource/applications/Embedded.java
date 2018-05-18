package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.Broadcaster;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.util.Logging;
import io.vertx.core.Vertx;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;

/**
 * Created by bberman on 11/29/16.
 */
public class Embedded {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("vertx.disableDnsResolver", "true");
		Logging.setLoggingLevel();
		Logger logger = LoggerFactory.getLogger(Embedded.class);

		final Vertx vertx = Vertx.vertx();
		mongo().connectWithEnvironment(vertx);
		Spellsource.spellsource().migrate(vertx, then -> {
			if (then.succeeded()) {
				Spellsource.spellsource().deployAll(vertx, deployed -> {
					if (deployed.failed()) {
						System.err.println("Failed to deploy due to an error: " + deployed.cause().getMessage());
						System.err.println("Stacktrace:");
						ExceptionUtils.printRootCauseStackTrace(deployed.cause());
					} else {
						// Deploy the broadcaster so that the client knows we're running local.
						vertx.deployVerticle(Broadcaster.create(), thenFinally -> {
							if (thenFinally.succeeded()) {
								logger.info("main: Server is ready.");
							} else {
								logger.info("main: The broadcasting agent failed to start. You will not be able to connect with a local client.");
							}
						});
					}
				});
			} else {
				logger.error("main: Failed to migrate, deployment aborted.");
				ExceptionUtils.printRootCauseStackTrace(then.cause());
			}
		});
	}
}

