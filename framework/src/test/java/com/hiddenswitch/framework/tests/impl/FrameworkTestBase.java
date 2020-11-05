package com.hiddenswitch.framework.tests.impl;

import com.hiddenswitch.containers.*;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Gateway;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.web.VertxWebClientExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@ExtendWith({VertxExtension.class, VertxWebClientExtension.class})
@Testcontainers
public class FrameworkTestBase {

	protected static final String PGDATABASE = "spellsource";
	protected static final String CLIENT_SECRET = "clientsecret";
	protected static final String CLIENT_ID = "spellsource";
	protected static final String PGUSER = "admin";
	protected static final String PGPASSWORD = "password";
	protected static final String PGHOST = "postgres";
	protected static final int PGPORT = 5432;

	protected static AtomicBoolean started = new AtomicBoolean(false);

	protected static RedisContainer redis = new RedisContainer()
			.withReuse(false);

	protected static PostgresSupabaseContainer postgres = new PostgresSupabaseContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withReuse(false)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PGPORT);

	protected static KeycloakContainer keycloak = new KeycloakContainer("jboss/keycloak:11.0.2")
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD)
			.withReuse(false);

	protected static RealtimeContainer realtime = new RealtimeContainer("realtimesecret", 4000)
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withEnv("DB_HOST", PGHOST)
			.withEnv("DB_NAME", PGDATABASE)
			.withEnv("DB_USER", PGUSER)
			.withEnv("DB_PASSWORD", PGPASSWORD)
			.withEnv("DB_PORT", Integer.toString(PGPORT))
			.withReuse(false);

	protected static OpenMatchContainer openMatch = new OpenMatchContainer()
			.dependsOn(redis)
			.withNetwork(Network.SHARED)
			.withReuse(false);

	@BeforeAll
	protected static void startContainers(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		var startup = Future.<Void>succeededFuture();

		if (started.compareAndSet(false, true)) {
			startup = Environment
					.executeBlocking(() -> Startables.deepStart(Stream.of(redis, postgres, keycloak, openMatch)).join())
					.compose(ignored -> Environment.executeBlocking(() -> {
						// Connect OpenMatch to redis
						var config = openMatch.getConfig();
						config.setRedis(new OpenMatchContainer.OpenMatchOverrideConfig.Redis()
								.setHostname(redis.getHost())
								.setPort(redis.getPortInsideNetwork())
								.setUser("default"));
						openMatch.setConfig(config);
						return null;
					}))
					.compose(ignored -> {
						// Set the configuration (typed)
						var serverConfiguration = ServerConfiguration.newBuilder()
								.setPg(ServerConfiguration.PostgresConfiguration.newBuilder()
										.setPort(postgres.getMappedPort(PGPORT))
										.setHost(postgres.getHost())
										.setDatabase(PGDATABASE)
										.setUser(PGUSER)
										.setPassword(PGPASSWORD)
										.build())
								.setKeycloak(ServerConfiguration.KeycloakConfiguration.newBuilder()
										.setAuthUrl(keycloak.getAuthServerUrl())
										.setAdminUsername(keycloak.getAdminUsername())
										.setAdminPassword(keycloak.getAdminPassword())
										.setClientId(CLIENT_ID)
										.setClientSecret(CLIENT_SECRET)
										.setRealmDisplayName("Spellsource")
										.setRealmId("hiddenswitch")
										.build())
								.setOpenmatch(ServerConfiguration.OpenmatchConfiguration.newBuilder()
										.setHost(openMatch.getHost())
										.setPort(openMatch.getPort())
										.build())
								.build();

						Environment.setConfiguration(serverConfiguration);
						return Future.succeededFuture();
					})
					.compose(ignored -> Environment.migrate())
					.compose(count -> {
						if (count < 4) {
							return Future.failedFuture(new RuntimeException(Integer.toString(count)));
						}
						return Future.succeededFuture(count);
					})
					.compose(ignored -> Environment.executeBlocking(() -> Startables.deepStart(Stream.of(realtime)).join()))
					.compose(ignored -> {
						// inject the realtime setup
						Environment.setConfiguration(ServerConfiguration.newBuilder()
								.setRealtime(ServerConfiguration.RealtimeConfiguration.newBuilder()
										.setUri(realtime.getRealtimeUrl()).build()).build());
						return Future.succeededFuture();
					});
		}

		startup
				.compose(ignored -> {
					// Start the application
					var promise = Promise.<String>promise();
					vertx.deployVerticle(Gateway::new, new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors() * 2), promise);
					return promise.future();
				})
				.onComplete(testContext.completing());
	}
}
