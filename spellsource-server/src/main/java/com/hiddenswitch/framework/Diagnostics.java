package com.hiddenswitch.framework;

import com.google.common.base.Throwables;
import com.google.protobuf.Empty;
import com.hiddenswitch.diagnostics.Tracing;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.sqlclient.SqlClient;
import org.redisson.api.redisnode.RedisNodes;

import java.util.concurrent.TimeUnit;

public class Diagnostics {

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

					var protos = client.unauthenticated().getConfiguration(Empty.getDefaultInstance()).eventually(client::close);
					var redis = Future.fromCompletionStage(Environment.redisson().getRedisNodes(RedisNodes.SINGLE).getInstance().pingAsync(200, TimeUnit.MILLISECONDS));
					var sql = Environment.sqlPoolAkaDaoDelegate().getConnection().compose(SqlClient::close);

					CompositeFuture.all(redis, sql, protos)
							.onSuccess(v1 -> {
								routingContext.end(Buffer.buffer("OK"));
							})
							.onFailure(t -> {
								routingContext.response().setStatusCode(500);
								routingContext.end(Buffer.buffer("could not connect to SQL server with error:\n" + Throwables.getStackTraceAsString(t)));
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
