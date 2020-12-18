package com.hiddenswitch.framework;

import com.hiddenswitch.containers.KeycloakContainer;
import com.hiddenswitch.containers.PostgresSupabaseContainer;
import com.hiddenswitch.containers.RealtimeContainer;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static io.vertx.core.CompositeFuture.all;

public class Application {
	private static Logger LOGGER = LoggerFactory.getLogger(Application.class);
	protected static final String PGDATABASE = "spellsource";
	public static final String CLIENT_SECRET = "clientsecret";
	public static final String CLIENT_ID = "spellsource";
	protected static final String PGUSER = "admin";
	protected static final String PGPASSWORD = "password";
	protected static final String PGHOST = "postgres";
	protected static final int PGPORT = 5432;

	protected static PostgresSupabaseContainer POSTGRES = new PostgresSupabaseContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withReuse(false)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PGPORT);

	public static RealtimeContainer REALTIME = new RealtimeContainer("realtimesecret", 4000)
			.dependsOn(POSTGRES)
			.withNetwork(Network.SHARED)
			.withEnv("DB_HOST", PGHOST)
			.withEnv("DB_NAME", PGDATABASE)
			.withEnv("DB_USER", PGUSER)
			.withEnv("DB_PASSWORD", PGPASSWORD)
			.withEnv("DB_PORT", Integer.toString(PGPORT))
			.withReuse(false);

	public static KeycloakContainer KEYCLOAK = new KeycloakContainer("jboss/keycloak:11.0.3")
			.dependsOn(POSTGRES)
			.withNetwork(Network.SHARED)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD);

	protected static AtomicBoolean STARTED = new AtomicBoolean(false);

	public static void main(String[] args) {
		defaultConfigurationAndServices();
		var configuration = Environment.cachedConfigurationOrGet();
		var vertx = Vertx.vertx();
		var broadcaster = configuration.getApplication().getUseBroadcaster() ? vertx.deployVerticle(Broadcaster.class, new DeploymentOptions().setInstances(1)) : Future.succeededFuture("");
		all(vertx.deployVerticle(Gateway.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
				vertx.deployVerticle(Matchmaking.class, new DeploymentOptions().setInstances(1)),
				vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions().setInstances(CpuCoreSensor.availableProcessors() * 2)),
				broadcaster)
				.onFailure(t -> LOGGER.error("main: failed with", t))
				.onSuccess(v -> LOGGER.info("main: Started application, now broadcasting"));
	}

	public static boolean defaultConfigurationAndServices() {
		var shouldStart = STARTED.compareAndSet(false, true);
		if (!shouldStart) {
			return false;
		}

		var configuration = ServerConfiguration.newBuilder(Environment.cachedConfigurationOrGet());
		var pgEnvVars = Arrays.asList("PGUSER", "PGPASSWORD", "PGHOST", "PGPORT", "PGDATABASE");
		// no pg var
		if (!configuration.hasPg() && pgEnvVars.stream().noneMatch(envVar -> System.getenv().containsKey(envVar))) {
			Startables.deepStart(Stream.of(POSTGRES)).join();
			configuration.setPg(ServerConfiguration.PostgresConfiguration.newBuilder()
					.setPort(POSTGRES.getMappedPort(PGPORT))
					.setHost(POSTGRES.getHost())
					.setDatabase(PGDATABASE)
					.setUser(PGUSER)
					.setPassword(PGPASSWORD)
					.build());
		}
		if (!configuration.hasKeycloak()) {
			Startables.deepStart(Stream.of(KEYCLOAK)).join();
			configuration.setKeycloak(ServerConfiguration.KeycloakConfiguration.newBuilder()
					.setAuthUrl(KEYCLOAK.getAuthServerUrl())
					.setAdminUsername(KEYCLOAK.getAdminUsername())
					.setAdminPassword(KEYCLOAK.getAdminPassword())
					.setClientId(CLIENT_ID)
					.setClientSecret(CLIENT_SECRET)
					.setRealmDisplayName("Spellsource")
					.setRealmId("hiddenswitch")
					.build());
		}
		if (!configuration.hasGrpcConfiguration()) {
			configuration.setGrpcConfiguration(ServerConfiguration.GrpcConfiguration.newBuilder()
					.setServerKeepAliveTimeMillis(400)
					.setServerKeepAliveTimeoutMillis(8000)
					.setServerPermitKeepAliveWithoutCalls(true)
					.build());
		}
		if (!configuration.hasMatchmaking()) {
			configuration.setMatchmaking(ServerConfiguration.MatchmakingConfiguration.newBuilder()
					.setMaxTicketsToProcess(100)
					.setScanFrequencyMillis(1200)
					.setEnqueueLockTimeoutMillis(400).build());
		}
		if (!configuration.hasApplication()) {
			configuration.setApplication(ServerConfiguration.ApplicationConfiguration.newBuilder()
					.setUseBroadcaster(true)
					.build());
		}

		// set the configuration so far so that migrations pick up on it
		Environment.setConfiguration(configuration.buildPartial());
		Environment.migrate().toCompletionStage().toCompletableFuture().join();

		if (!configuration.hasRealtime()) {
			Startables.deepStart(Stream.of(REALTIME)).join();
			configuration.setRealtime(ServerConfiguration.RealtimeConfiguration.newBuilder()
					.setUri(REALTIME.getRealtimeUrl()).build());
		}

		Environment.setConfiguration(configuration.build());
		return true;
	}
}
