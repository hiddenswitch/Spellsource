package com.hiddenswitch.framework.tests.applications;

import com.hiddenswitch.containers.GraphQLContainer;
import com.hiddenswitch.containers.KeycloakContainer;
import com.hiddenswitch.containers.PersistentNetwork;
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
import org.testcontainers.utility.TestcontainersConfiguration;

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
	// when the environment enables testcontainers reuse (TESTCONTAINERS_REUSE_ENABLE=true, set by
	// gradlew spellsource-server:run, or testcontainers.reuse.enable in ~/.testcontainers.properties),
	// postgres, keycloak and redis survive JVM exit and are reattached by subsequent runs. they must
	// live on a network whose id is stable across JVMs (Network.SHARED is recreated per JVM and would
	// invalidate the reuse hash). GraphQL runs nodemon against the working tree, so it can safely
	// be reused too while restarting only the Java server.
	protected static final boolean REUSE = TestcontainersConfiguration.getInstance().environmentSupportsReuse();
	protected static final String REUSE_LABEL = "com.hiddenswitch.spellsource";
	protected static final Network NETWORK = REUSE ? new PersistentNetwork("spellsource-dev") : Network.SHARED;
	public static RedisContainer REDIS = new RedisContainer()
			.withNetwork(NETWORK)
			.withLabel(REUSE_LABEL, "localdev")
			.withReuse(REUSE);
	protected static PostgresContainer POSTGRES = new PostgresContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withNetwork(NETWORK)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PostgresContainer.POSTGRESQL_PORT)
			.withLabel(REUSE_LABEL, "localdev")
			.withReuse(REUSE);
	public static KeycloakContainer KEYCLOAK = new KeycloakContainer()
			.dependsOn(POSTGRES)
			.withNetwork(NETWORK)
			.withNetworkAliases(KEYCLOAK_HOST)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD)
			.withLabel(REUSE_LABEL, "localdev")
			.withReuse(REUSE);
	public static GraphQLContainer GRAPHQL = new GraphQLContainer()
			.dependsOn(POSTGRES)
			.withNetwork(NETWORK)
			.withNetworkAliases(GRAPHQL_HOST)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD)
			.withKeycloak(KEYCLOAK_HOST, KeycloakContainer.KEYCLOAK_PORT_HTTP)
			.withLabel(REUSE_LABEL, "localdev")
			.withReuse(REUSE);
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
				.setUrl(String.format("http://%s:%d/graphql", "localhost", GRAPHQL.getMappedPort(GraphQLContainer.GRAPHQL_PORT)))
		);
		// todo: allow environment variables *only* to override this configuration, but something weird about kube env
		// configuration.mergeFrom(Environment.environmentConfiguration());

		// set the configuration so far so that migrations pick up on it
		Environment.setConfiguration(configuration.buildPartial());
		Environment.migrate().toCompletionStage().toCompletableFuture().join();

		LOGGER.info("Keycloak address is http://localhost:{}", KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP));
		LOGGER.info("Redis address is {}", REDIS.getRedisUrl());
		LOGGER.info("Postgres address is {}", POSTGRES.getHostAndPort());
		Environment.setConfiguration(configuration.build());
		try {
			var envFile = new File("../spellsource-web/.env.local");
			var contents = "REDIS_URI=" + REDIS.getRedisUrl() + "\n"
					+ "KEYCLOAK_PORT=" + KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP) + "\n"
					+ "NEXT_PUBLIC_GRAPHQL_PORT=" + GRAPHQL.getMappedPort(GraphQLContainer.GRAPHQL_PORT) + "\n";
			FileUtils.writeStringToFile(envFile, contents, StandardCharsets.UTF_8);

			var envFile2 = new File("../spellsource-graphql/.env.local");
			var contents2 = "KEYCLOAK_ISSUER=http://localhost:" + KEYCLOAK.getMappedPort(KeycloakContainer.KEYCLOAK_PORT_HTTP) + "/realms/hiddenswitch";
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
