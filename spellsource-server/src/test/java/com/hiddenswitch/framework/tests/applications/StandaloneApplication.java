package com.hiddenswitch.framework.tests.applications;

import com.hiddenswitch.containers.GraphQLContainer;
import com.hiddenswitch.containers.KeycloakContainer;
import com.hiddenswitch.containers.PostgresContainer;
import com.hiddenswitch.containers.RedisContainer;
import com.hiddenswitch.framework.Application;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.rpc.Hiddenswitch.ServerConfiguration;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class StandaloneApplication extends Application {
	public static final String CLIENT_SECRET = "clientsecret";
	public static final String CLIENT_ID = "spellsource";
	protected static final String PGDATABASE = "spellsource";
	protected static final String PGUSER = "admin";
	protected static final String PGPASSWORD = "password";
	protected static final String PGHOST = "postgres";
	protected static final String GRAPHQL_HOST = "graphql";
	protected static final String KEYCLOAK_HOST = "keycloak";
	private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneApplication.class);
	public static RedisContainer REDIS = new RedisContainer()
			.withNetwork(Network.SHARED)
			.withReuse(true);
	protected static PostgresContainer POSTGRES = new PostgresContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withReuse(true)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PostgresContainer.POSTGRESQL_PORT);
	public static KeycloakContainer KEYCLOAK = new KeycloakContainer()
			.withReuse(true)
			.dependsOn(POSTGRES)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(KEYCLOAK_HOST)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD);
	public static GraphQLContainer GRAPHQL = new GraphQLContainer()
			.withReuse(true)
			.dependsOn(POSTGRES)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(GRAPHQL_HOST)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD)
			.withKeycloak(KEYCLOAK_HOST, KeycloakContainer.KEYCLOAK_PORT_HTTP);
	protected static AtomicBoolean STARTED = new AtomicBoolean(false);

	public static boolean defaultConfigurationAndServices() {
		var shouldStart = STARTED.compareAndSet(false, true);
		if (!shouldStart) {
			return false;
		}
		Startables.deepStart(Stream.of(POSTGRES, KEYCLOAK, REDIS, GRAPHQL)).join();
		var configuration = ServerConfiguration.newBuilder(Environment.getConfiguration());
		configuration.setPg(ServerConfiguration.PostgresConfiguration.newBuilder()
				.setPort(POSTGRES.getMappedPort(PostgresContainer.POSTGRESQL_PORT))
				.setHost(POSTGRES.getHost())
				.setDatabase(PGDATABASE)
				.setUser(PGUSER)
				.setPassword(PGPASSWORD)
				.build());
		configuration.setKeycloak(ServerConfiguration.KeycloakConfiguration.newBuilder()
				.setAuthUrl(KEYCLOAK.getAuthServerUrl())
				.setAdminUsername(KEYCLOAK.getAdminUsername())
				.setAdminPassword(KEYCLOAK.getAdminPassword())
				.setClientId(CLIENT_ID)
				.setClientSecret(CLIENT_SECRET)
				.setRealmDisplayName("Spellsource")
				.setRealmId("hiddenswitch")
				.build());
		configuration.setRedis(ServerConfiguration.RedisConfiguration.newBuilder()
				.setUri(REDIS.getRedisUrl())
				.build());
		configuration.setApplication(ServerConfiguration.ApplicationConfiguration.newBuilder()
				.setUseBroadcaster(true)
				.build());
		configuration.setMigration(ServerConfiguration.MigrationConfiguration.newBuilder()
				.setShouldMigrate(true)
				.build());
		configuration.setGraphql(ServerConfiguration.GraphQLConfiguration.newBuilder()
				.setHost(GRAPHQL_HOST)
				.setPort(GRAPHQL.getMappedPort(GraphQLContainer.GRAPHQL_PORT))
				.setRoute("/graphql")
		);
		// todo: allow environment variables *only* to override this configuration, but something weird about kube env
		// configuration.mergeFrom(Environment.environmentConfiguration());

		// set the configuration so far so that migrations pick up on it
		Environment.setConfiguration(configuration.buildPartial());
		Environment.migrate().toCompletionStage().toCompletableFuture().join();

		LOGGER.info("StandaloneApplication: Keycloak address is http://localhost:{}", KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP));
		LOGGER.info("StandaloneApplication: Redis address is {}", REDIS.getRedisUrl());
		LOGGER.info("StandaloneApplication: Postgres address is {}", POSTGRES.getHostAndPort());
		Environment.setConfiguration(configuration.build());
		try {
			var envFile = new File("../spellsource-web/.env.local");
			var contents = STR."""
REDIS_URI=\{REDIS.getRedisUrl()}
PG_PORT=\{POSTGRES.getMappedPort(PostgresContainer.POSTGRESQL_PORT)}
KEYCLOAK_PORT=\{KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP)}
NEXT_PUBLIC_GRAPHQL_PORT=\{GRAPHQL.getMappedPort(GraphQLContainer.GRAPHQL_PORT)}
""";
			FileUtils.writeStringToFile(envFile, contents, StandardCharsets.UTF_8);

			var envFile2 = new File("../spellsource-graphql/.env.local");
			var contents2 = STR."""
KEYCLOAK_ISSUER=http://localhost:\{KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP)}/realms/hiddenswitch
""";
			FileUtils.writeStringToFile(envFile2, contents2, StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			LOGGER.error("Error occurred while writing the environment file for the website.");
		}

		return true;
	}

	@Override
	protected Future<Vertx> getVertx() {
		return Future.succeededFuture(Vertx.vertx(Environment.vertxOptions()));
	}

	@Override
	protected Future<Vertx> deploy(Vertx vertx) {
		defaultConfigurationAndServices();
		return super.deploy(vertx);
	}
}
