package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.Broadcaster;
import com.hiddenswitch.spellsource.Spellsource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;

/**
 * Created by bberman on 11/29/16.
 */
public class Embedded {
	public static void main(String args[]) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("vertx.disableDnsResolver", "true");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);

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
								System.out.println("Server is ready.");
							} else {
								System.err.println("The broadcasting agent failed to start. You will not be able to connect with a local client.");
							}
						});
					}
				});
			} else {
				System.err.println("Failed to migrate, deployment aborted.");
			}
		});
	}
}

