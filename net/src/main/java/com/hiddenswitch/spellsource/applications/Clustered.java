package com.hiddenswitch.spellsource.applications;

import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.util.Logging;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.atomix.core.Atomix;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * The main entry point of the game server.
 * <p>
 * Starts a clustered service, then tries to migrate the database.
 */
public class Clustered {
	public static void main(String args[]) {
		Applications.startServer(vertx -> {
		});
	}
}

