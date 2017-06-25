package com.hiddenswitch.proto3.net.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.minionate.Minionate;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Migrations;
import com.hiddenswitch.proto3.net.impl.DecksImpl;
import com.hiddenswitch.proto3.net.models.DeckListUpdateRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.UpdateOptions;

import static com.hiddenswitch.proto3.net.util.Mongo.mongo;
import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

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
		Minionate.minionate().migrate(vertx, then -> {
			if (then.succeeded()) {
				Minionate.minionate().deployAll(vertx, Future.future());
			} else {
				System.err.println("Failed to migrate, deployment aborted.");
			}
		});
	}
}

