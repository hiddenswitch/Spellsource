package com.hiddenswitch.framework;

import com.google.common.base.Throwables;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.protobuf.Empty;
import com.hiddenswitch.diagnostics.Tracing;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diagnostics {
	private final static Logger LOGGER = LoggerFactory.getLogger(Diagnostics.class);
	private final static Multiset<String> MESSAGES = ConcurrentHashMultiset.create();

	public static void post(String message) {
		MESSAGES.add(message);
	}

	public static void post(String message, int count) {
		MESSAGES.add(message, count);
	}

	public static Future<Void> routes(Vertx vertx) {
		var configuration = Environment.getConfiguration();
		var router = Router.router(vertx);
		router.get(configuration.getMetrics().getLivenessRoute())
				.handler(routingContext -> routingContext.end(Buffer.buffer("OK")));

		router.get(configuration.getMetrics().getReadinessRoute())
				.handler(routingContext -> {
					// can I handle an account login?
					// can I fetch a deck?
					// can I create a game?
					// for now, we'll just check that we can connect to the SQL server & redis
					// keycloak should be managed with its own readiness probe
					// TODO: what should the redis nodes selection here be?
					var client = new Client(vertx);

					var protos = client.unauthenticated().getConfiguration(Empty.getDefaultInstance()).eventually(client::closeFut);
					// Works for single-server, sentinel, cluster, and replication — issues a lightweight
					// EXISTS against an arbitrary key. Avoids Redisson.getRedisNodes() which is typed per
					// topology and throws on the wrong configuration.
					var redis = Future.fromCompletionStage(Environment.redisson().getBucket("_healthz").isExistsAsync().toCompletableFuture());
					var pgConnectOptions = Environment.pgArgs().connectionOptions();
					var pgClient = PgBuilder.pool().using(vertx).connectingTo(pgConnectOptions).with(new PoolOptions()).build();
					var postgres = pgClient.query("""
									select success
									     from hiddenswitch.flyway_schema_history
									     order by installed_rank desc
									     limit 1
									     """)
							.execute()
							.compose(res -> res.size() == 0 ? Future.failedFuture("migration not complete") : Future.succeededFuture())
							.eventually(pgClient::close);

					Future.all(redis, protos, postgres)
							.onSuccess(v -> routingContext.end(Buffer.buffer("OK")))
							.onFailure(t -> {
								routingContext.response().setStatusCode(500);
								routingContext.end(Buffer.buffer("health check failed with error:\n" + Throwables.getStackTraceAsString(t)));
							});
				});

		router.get(configuration.getMetrics().getMetricsRoute())
				.handler(io.vertx.micrometer.PrometheusScrapingHandler.create(Environment.prometheusMeterRegistry()));

		var server = vertx.createHttpServer(new HttpServerOptions().setPort(configuration.getMetrics().getPort()));
		server.requestHandler(router);
		return server.listen().mapEmpty();
	}

	public static Future<Void> tracing(Vertx vertx) {
		return Future.succeededFuture(Tracing.tracing(vertx)).mapEmpty();
	}
}
