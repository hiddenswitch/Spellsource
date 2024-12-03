package com.hiddenswitch.framework;

import com.google.common.base.Throwables;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.protobuf.Empty;
import com.hiddenswitch.diagnostics.Tracing;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.redisson.api.redisnode.RedisNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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

					var protos = client.unauthenticated().getConfiguration(Empty.getDefaultInstance()).eventually(v -> client.closeFut());
					var redis = Future.fromCompletionStage(Environment.redisson().getRedisNodes(RedisNodes.SINGLE).getInstance().pingAsync(200, TimeUnit.MILLISECONDS));
					var pgConnectOptions = Environment.pgArgs().connectionOptions();
					pgConnectOptions.setConnectTimeout(1000);
					var pgClient = PgPool.client(vertx, pgConnectOptions, new PoolOptions());
					var postgres = pgClient.query("""
									select success
									     from hiddenswitch.flyway_schema_history
									     order by installed_rank desc
									     limit 1
									     """)
							.execute()
							.compose(res -> res.size() == 0 ? Future.failedFuture("migration not complete") : Future.succeededFuture())
							.eventually(v -> pgClient.close());

					Future.all(redis, protos, postgres)
							.onSuccess(v -> routingContext.end(Buffer.buffer("OK")))
							.onFailure(t -> {
								routingContext.response().setStatusCode(500);
								routingContext.end(Buffer.buffer("health check failed with error:\n" + Throwables.getStackTraceAsString(t)));
							});
				});

		router.get(configuration.getMetrics().getMetricsRoute())
				.handler(routingContext -> {
					var registry = (PrometheusMeterRegistry) Environment.registry().getMeterRegistry();
					routingContext.response()
							.putHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
							.end(registry.scrape());
				});

		var server = vertx.createHttpServer(new HttpServerOptions().setPort(configuration.getMetrics().getPort()));
		server.requestHandler(router);
		return server.listen().mapEmpty();
	}

	public static Future<Void> tracing(Vertx vertx) {
		return Future.succeededFuture(Tracing.tracing(vertx)).mapEmpty();
	}
}
