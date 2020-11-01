package com.hiddenswitch.framework.tests;

import com.hiddenswitch.containers.KeycloakContainer;
import com.hiddenswitch.containers.PostgresSupabaseContainer;
import com.hiddenswitch.containers.RealtimeContainer;
import com.hiddenswitch.containers.RedisContainer;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Gateway;
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

	protected static AtomicBoolean started = new AtomicBoolean(false);
	protected static final String CLIENT_SECRET = "clientsecret";
	protected static final String CLIENT_ID = "spellsource";
	protected static RedisContainer redis = new RedisContainer()
			.withReuse(false);

	protected static PostgresSupabaseContainer postgres = new PostgresSupabaseContainer("admin", "password", "spellsource")
			.withReuse(false)
			.withNetwork(Network.SHARED)
			.withNetworkAliases("postgres")
			.withExposedPorts(5432);

	protected static KeycloakContainer keycloak = new KeycloakContainer("jboss/keycloak")
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withPostgres("postgres", "spellsource", "admin", "password")
			.withReuse(false);

	protected static RealtimeContainer realtime = new RealtimeContainer("realtimesecret", 4000)
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withEnv("DB_HOST", "postgres")
			.withEnv("DB_NAME", "spellsource")
			.withEnv("DB_USER", "admin")
			.withEnv("DB_PASSWORD", "password")
			.withEnv("DB_PORT", Integer.toString(5432))
			.withReuse(false);

	@BeforeAll
	protected static void startContainers(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
		var startup = Future.<Void>succeededFuture();

		if (started.compareAndSet(false, true)) {
			startup = Environment
					.executeBlocking(() -> Startables.deepStart(Stream.of(redis, postgres, keycloak)).join())
					.compose(ignored -> {
						// inject the postgres connection parameters
						System.getProperties().putIfAbsent("pg.port", Integer.toString(postgres.getMappedPort(5432)));
						System.getProperties().putIfAbsent("pg.host", postgres.getHost());
						System.getProperties().putIfAbsent("pg.database", "spellsource");
						System.getProperties().putIfAbsent("pg.user", "admin");
						System.getProperties().putIfAbsent("pg.password", "password");
						// inject the keycloak parameters
						System.getProperties().putIfAbsent("keycloak.auth.url", keycloak.getAuthServerUrl());
						System.getProperties().putIfAbsent("keycloak.admin.username", keycloak.getAdminUsername());
						System.getProperties().putIfAbsent("keycloak.admin.password", keycloak.getAdminPassword());
						System.getProperties().putIfAbsent("keycloak.client.id", CLIENT_ID);
						System.getProperties().putIfAbsent("keycloak.client.secret", CLIENT_SECRET);
						System.getProperties().putIfAbsent("keycloak.realm.display.name", "Spellsource");

						return Future.succeededFuture();
					})
					.compose(ignored -> Environment.migrate("jdbc:postgresql://" + postgres.getHostAndPort() + "/spellsource", "admin", "password"))
					.compose(count -> {
						if (count < 4) {
							return Future.failedFuture(new RuntimeException(Integer.toString(count)));
						}
						return Future.succeededFuture(count);
					})
					.compose(ignored -> Environment.executeBlocking(() -> Startables.deepStart(Stream.of(realtime)).join()))
					.compose(ignored -> {
						// inject the realtime setup
						System.getProperties().putIfAbsent("realtime.uri", realtime.getRealtimeUrl());
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
