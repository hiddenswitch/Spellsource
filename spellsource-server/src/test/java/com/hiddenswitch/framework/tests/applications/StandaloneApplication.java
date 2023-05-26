package com.hiddenswitch.framework.tests.applications;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.hiddenswitch.containers.*;
import com.hiddenswitch.framework.Application;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.commons.io.FileUtils;
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
	protected static final int PGPORT = 5432;
	protected static PostgresSupabaseContainer POSTGRES = new PostgresSupabaseContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withReuse(true)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PGPORT);
	public static KeycloakContainer KEYCLOAK = new KeycloakContainer()
			.withReuse(true)
			.dependsOn(POSTGRES)
			.withNetwork(Network.SHARED)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD);

	public static RedisContainer REDIS = new RedisContainer()
			.withNetwork(Network.SHARED)
			.withReuse(true);
	protected static AtomicBoolean STARTED = new AtomicBoolean(false);

	@Override
	protected Future<Vertx> getVertx() {
		return Future.succeededFuture(Vertx.vertx(Environment.vertxOptions()));
	}

	public static boolean defaultConfigurationAndServices() {
		var shouldStart = STARTED.compareAndSet(false, true);
		if (!shouldStart) {
			return false;
		}
		Startables.deepStart(Stream.of(POSTGRES, KEYCLOAK, REDIS)).join();
		var configuration = ServerConfiguration.newBuilder(Environment.getConfiguration());
		configuration.setPg(ServerConfiguration.PostgresConfiguration.newBuilder()
				.setPort(POSTGRES.getMappedPort(PGPORT))
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
		// todo: allow environment variables *only* to override this configuration, but something weird about kube env
		// configuration.mergeFrom(Environment.environmentConfiguration());

		// set the configuration so far so that migrations pick up on it
		Environment.setConfiguration(configuration.buildPartial());
		Environment.migrate().toCompletionStage().toCompletableFuture().join();

		Environment.setConfiguration(configuration.build());


        try {
            var envFile = new File("../spellsource-web/.env.local");
            var contents = "PG_PORT=" + POSTGRES.getMappedPort(PGPORT) + "\nKEYCLOAK_PORT=" + KEYCLOAK.getMappedPort(8080);

            FileUtils.writeStringToFile(envFile, contents, StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.err.println(e);
        }

		return true;
	}

	@Override
	protected Future<Vertx> deploy(Vertx vertx) {
		defaultConfigurationAndServices();
		return super.deploy(vertx);
	}
}
