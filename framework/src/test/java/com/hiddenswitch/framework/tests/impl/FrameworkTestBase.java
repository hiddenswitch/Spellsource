package com.hiddenswitch.framework.tests.impl;

import com.hiddenswitch.containers.*;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Gateway;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.*;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.vertx.core.CompositeFuture.all;
import static org.testcontainers.Testcontainers.exposeHostPorts;

@ExtendWith({VertxExtension.class})
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

	protected static PostgresSupabaseContainer postgres = new PostgresSupabaseContainer(PGUSER, PGPASSWORD, PGDATABASE)
			.withReuse(false)
			.withNetwork(Network.SHARED)
			.withNetworkAliases(PGHOST)
			.withExposedPorts(PGPORT);

	protected static KeycloakContainer keycloak = new KeycloakContainer("jboss/keycloak:11.0.3")
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withPostgres(PGHOST, PGDATABASE, PGUSER, PGPASSWORD);

	protected static RealtimeContainer realtime = new RealtimeContainer("realtimesecret", 4000)
			.dependsOn(postgres)
			.withNetwork(Network.SHARED)
			.withEnv("DB_HOST", PGHOST)
			.withEnv("DB_NAME", PGDATABASE)
			.withEnv("DB_USER", PGUSER)
			.withEnv("DB_PASSWORD", PGPASSWORD)
			.withEnv("DB_PORT", Integer.toString(PGPORT))
			.withReuse(false);

	protected static ToxiproxyContainer toxiproxy = new ToxiproxyContainer("shopify/toxiproxy:2.1.4");

	private static ToxiproxyContainer.ContainerProxy toxicGrpcProxy;

	public static ToxiproxyContainer.ContainerProxy toxicGrpcProxy() {
		return toxicGrpcProxy;
	}

	@BeforeAll
	protected static void startContainers() throws InterruptedException {
		var shouldStart = started.compareAndSet(false, true);
		if (!shouldStart) {
			return;
		}

		exposeHostPorts(Gateway.grpcPort());
		Startables.deepStart(Stream.of(postgres, keycloak, toxiproxy)).join();
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
				.setGrpcConfiguration(ServerConfiguration.GrpcConfiguration.newBuilder()
						.setServerKeepAliveTimeMillis(400)
						.setServerKeepAliveTimeoutMillis(8000)
						.setServerPermitKeepAliveWithoutCalls(true)
						.build())
				.setMatchmaking(ServerConfiguration.MatchmakingConfiguration.newBuilder()
						.setMaxTicketsToProcess(100)
						.setScanFrequencyMillis(1200)
						.setEnqueueLockTimeoutMillis(400).build())
				.build();

		Environment.setConfiguration(serverConfiguration);
		Environment.migrate().toCompletionStage().toCompletableFuture().join();
		Startables.deepStart(Stream.of(realtime)).join();

		Environment.setConfiguration(ServerConfiguration.newBuilder()
				.setRealtime(ServerConfiguration.RealtimeConfiguration.newBuilder()
						.setUri(realtime.getRealtimeUrl()).build()).build());
		toxicGrpcProxy = toxiproxy.getProxy("host.testcontainers.internal", Gateway.grpcPort());
	}


	protected Future<String> startGateway(Vertx vertx) {
		return vertx.deployVerticle(Gateway.class, new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors() * 2));
	}

	public static class Checkpoint implements Future<Void> {
		private int times;
		private Promise<Void> finished = Promise.promise();
		private Future<Void> future = finished.future();

		@Override
		public boolean isComplete() {
			return future.isComplete();
		}

		@Override
		@Fluent
		public Future<Void> onComplete(Handler<AsyncResult<Void>> handler) {
			return future.onComplete(handler);
		}

		@Override
		@Fluent
		public Future<Void> onSuccess(Handler<Void> handler) {
			return future.onSuccess(handler);
		}

		@Override
		@Fluent
		public Future<Void> onFailure(Handler<Throwable> handler) {
			return future.onFailure(handler);
		}

		@Override
		public Void result() {
			return future.result();
		}

		@Override
		public Throwable cause() {
			return future.cause();
		}

		@Override
		public boolean succeeded() {
			return future.succeeded();
		}

		@Override
		public boolean failed() {
			return future.failed();
		}

		@Override
		public <U> Future<U> flatMap(Function<Void, Future<U>> mapper) {
			return future.flatMap(mapper);
		}

		@Override
		public <U> Future<U> compose(Function<Void, Future<U>> mapper) {
			return future.compose(mapper);
		}

		@Override
		public Future<Void> recover(Function<Throwable, Future<Void>> mapper) {
			return future.recover(mapper);
		}

		@Override
		public <U> Future<U> compose(Function<Void, Future<U>> successMapper, Function<Throwable, Future<U>> failureMapper) {
			return future.compose(successMapper, failureMapper);
		}

		@Override
		public <U> Future<U> eventually(Function<Void, Future<U>> mapper) {
			return future.eventually(mapper);
		}

		@Override
		public <U> Future<U> map(Function<Void, U> mapper) {
			return future.map(mapper);
		}

		@Override
		public <V> Future<V> map(V value) {
			return future.map(value);
		}

		@Override
		public <V> Future<V> mapEmpty() {
			return future.mapEmpty();
		}

		@Override
		public Future<Void> otherwise(Function<Throwable, Void> mapper) {
			return future.otherwise(mapper);
		}

		@Override
		public Future<Void> otherwise(Void value) {
			return future.otherwise(value);
		}

		@Override
		public Future<Void> otherwiseEmpty() {
			return future.otherwiseEmpty();
		}

		@Override
		@GenIgnore
		public CompletionStage<Void> toCompletionStage() {
			return future.toCompletionStage();
		}

		@GenIgnore
		public static <T> Future<T> fromCompletionStage(CompletionStage<T> completionStage) {
			return Future.fromCompletionStage(completionStage);
		}

		@GenIgnore
		public static <T> Future<T> fromCompletionStage(CompletionStage<T> completionStage, Context context) {
			return Future.fromCompletionStage(completionStage, context);
		}

		private Checkpoint(int times) {
			this.times = times;
		}

		public Future<Void> flag() {
			this.times -= 1;
			if (this.times < 0) {
				return Future.failedFuture("flagged too many times");
			}

			if (this.times == 0) {
				finished.tryComplete();
			}
			return Future.succeededFuture();
		}

		public static Checkpoint checkpoint(int times) {
			return new Checkpoint(times);
		}

		public static Future<Void> awaitCheckpoints(Checkpoint... checkpoints) {
			return all(Arrays.asList(checkpoints)).map((Void) null);
		}
	}
}
