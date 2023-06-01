package com.hiddenswitch.framework;

import com.github.marschall.micrometer.jfr.JfrMeterRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.Streams;
import com.google.protobuf.Parser;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.impl.Clustered;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.impl.RedissonProtobufCodec;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.Hiddenswitch.ServerConfiguration;
import com.hiddenswitch.protos.Serialization;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.Metrics;
import io.opentracing.util.GlobalTracer;
import io.vertx.await.Async;
import io.vertx.core.Context;
import io.vertx.core.*;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.PrometheusBackendRegistry;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.Row;
import io.vertx.tracing.opentracing.OpenTracingOptions;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Query;
import org.jooq.impl.DefaultConfiguration;
import org.redisson.Redisson;
import org.redisson.api.RMapCacheAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.config.Config;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.hiddenswitch.protos.Serialization.configureSerialization;

public class Environment {
	private static final PrometheusBackendRegistry prometheusRegistry = new PrometheusBackendRegistry(new VertxPrometheusOptions()
			.setEnabled(true)
			.setPublishQuantiles(true));
	private static final AtomicBoolean prometheusInited = new AtomicBoolean();
	private static final AtomicReference<Configuration> jooqConfiguration = new AtomicReference<>();
	private static final WeakVertxMap<Async> asyncs = new WeakVertxMap<>(Environment::asyncConstructor);
	private static final String CONFIGURATION_NAME_TOKEN = "spellsource";
	private static final AtomicReference<ServerConfiguration> cachedConfiguration = new AtomicReference<>();
	private static ServerConfiguration programmaticConfiguration = ServerConfiguration.newBuilder().build();
	private static final WeakVertxMap<SqlClient> sqlClients = new WeakVertxMap<>(Environment::sharedClient);
	private static final WeakVertxMap<Pool> transactionPools = new WeakVertxMap<>(Environment::sharedPool);
	private static final WeakVertxMap<RedissonClient> redissonClients = new WeakVertxMap<>(Environment::redissonClientConstructor);

	static {
		// An opportunity to configure Vertx's JSON
		configureSerialization();
		// metrics
		metrics();
	}

	private static RedissonClient redissonClientConstructor(Vertx vertx) {
		var configuration = getConfiguration();
		var config = new Config();
		config.useSingleServer().setAddress(configuration.getRedis().getUri());
		return Redisson.create(config);
	}

	private static Async asyncConstructor(Vertx vertx) {
		return new Async(vertx, true);
	}

	private static PgPool pool(Vertx vertx) {
		var args = pgArgs();
		return PgPool.pool(vertx, args.connectionOptions(), args.poolOptions());
	}

	private static PgPool sharedPool(Vertx vertx) {
		var args = pgArgs();
		var options = sharedOptions("sharedPools__",vertx,args.poolOptions());
		return PgPool.pool(vertx, args.connectionOptions(), options);
	}

	private static SqlClient client(Vertx vertx) {
		var args = pgArgs();
		return PgPool.client(vertx, args.connectionOptions(), args.poolOptions());
	}

	private static PoolOptions sharedOptions(String prefix, Vertx vertx, PoolOptions options) {
		return options
				.setName(prefix + (vertx == null ? "null" : vertx.toString()))
				.setShared(true)
				.setEventLoopSize(CpuCoreSensor.availableProcessors());
	}

	private static SqlClient sharedClient(Vertx vertx) {
		var args = pgArgs();
		var options = sharedOptions("sharedClient__", vertx, args.poolOptions());
		return PgPool.client(vertx, args.connectionOptions(), options);
	}

	@NotNull
	private static PgArgs pgArgs() {
		var connectionOptions = PgConnectOptions.fromEnv();
		var cachedConfiguration = getConfiguration();
		if (cachedConfiguration.hasPg()) {
			var pg = cachedConfiguration.getPg();
			if (pg.getPort() != 0) {
				connectionOptions.setPort(pg.getPort());
			}
			if (!pg.getHost().isEmpty()) {
				connectionOptions.setHost(pg.getHost());
			}
			if (!pg.getPassword().isEmpty()) {
				connectionOptions.setPassword(pg.getPassword());

			}
			if (!pg.getDatabase().isEmpty()) {
				connectionOptions.setDatabase(pg.getDatabase());
			}
			if (!pg.getUser().isEmpty()) {
				connectionOptions.setUser(pg.getUser());
			}
			if (!pg.getPassword().isEmpty()) {
				connectionOptions.setPassword(pg.getPassword());
			}
		}

		var poolOptions = new PoolOptions()
				.setMaxSize(Math.max(CpuCoreSensor.availableProcessors(), 8));
		var args = new PgArgs(connectionOptions, poolOptions);
		return args;
	}

	public static void metrics() {
		if (prometheusInited.compareAndSet(false, true)) {
			Metrics.addRegistry(new JfrMeterRegistry());
			Metrics.addRegistry(prometheusRegistry.getMeterRegistry());
			prometheusRegistry.init();
		}
	}

	public static SqlClient sqlClient() {
		return sqlClients.get();
	}

	public static Pool transactionPool() {
		return transactionPools.get();
	}

	public static Async async() {
		return asyncs.get();
	}

	public static Future<Integer> withDslContext(Function<DSLContext, ? extends Query> handler) {
		return withExecutor(executor -> executor.execute(handler));
	}

	public static Future<RowSet<io.vertx.sqlclient.Row>> query(Function<DSLContext, ? extends Query> handler) {
		return withExecutor(executor -> executor.executeAny(handler));
	}

	public static <T> Future<T> withExecutor(Function<ReactiveClassicGenericQueryExecutor, Future<T>> handler) {
		var conn = Environment.sqlClient();
		var executor = new ReactiveClassicGenericQueryExecutor(Environment.jooqAkaDaoConfiguration(), conn);
		return handler.apply(executor);
	}

	public static <T> Future<T> withConnection(Function<SqlConnection, Future<T>> handler) {
		return Environment.transactionPool()
				.getConnection()
				.compose(conn -> handler.apply(conn)
						.onComplete(v -> conn.close()));
	}

	public static Handler<Throwable> onFailure() {
		return onFailure("");
	}

	public static Handler<Throwable> onFailure(String optionalMessage) {
		var here = new Throwable(optionalMessage);
		var span = GlobalTracer.get().activeSpan();
		return t -> {
			t.setStackTrace(concatAndFilterStackTrace(t, here));
			Tracing.error(t, span, true);
		};
	}

	public static StackTraceElement[] concatAndFilterStackTrace(Throwable... throwables) {
		var length = 0;
		for (var i = 0; i < throwables.length; i++) {
			length += throwables[i].getStackTrace().length;
		}
		var newStack = new ArrayList<StackTraceElement>(length);
		for (var throwable : throwables) {
			var stack = throwable.getStackTrace();
			for (var i = 0; i < stack.length; i++) {
				if (stack[i].getClassName().startsWith("co.paralleluniverse.fibers.") ||
						stack[i].getClassName().startsWith("co.paralleluniverse.strands.") ||
						stack[i].getClassName().startsWith("io.vertx.ext.sync.") ||
						stack[i].getClassName().startsWith("io.vertx.core.impl.future.") ||
						stack[i].getClassName().startsWith("io.vertx.core.Promise") ||
						stack[i].getClassName().startsWith("io.netty.") ||
						stack[i].getClassName().startsWith("io.vertx.core.impl.") ||
						stack[i].getClassName().startsWith("sun.nio.") ||
						stack[i].getClassName().startsWith("java.base/java.util.concurrent") ||
						stack[i].getClassName().startsWith("java.base/java.lang.Thread") ||
						stack[i].getClassName().startsWith("java.util.concurrent")) {
					continue;
				}
				newStack.add(stack[i]);
			}
		}
		return newStack.toArray(new StackTraceElement[0]);
	}

	public static Future<Void> sleep(Vertx vertx, long milliseconds) {
		if (vertx == null) {
			if (Vertx.currentContext() == null) {
				return Future.failedFuture(new IllegalArgumentException("not on context, can't sleep"));
			}
			vertx = Vertx.currentContext().owner();
		}
		var promise = Promise.<Long>promise();
		vertx.setTimer(milliseconds, promise::complete);
		return promise.future().mapEmpty();
	}

	public static Future<Void> sleep(long milliseconds) {
		return sleep(null, milliseconds);
	}

	public static Configuration jooqAkaDaoConfiguration() {
		return jooqConfiguration.updateAndGet(existing -> {
			if (existing != null) {
				return existing;
			}
			var defaultConfiguration = new DefaultConfiguration();
			defaultConfiguration.setSQLDialect(SQLDialect.POSTGRES);
			return defaultConfiguration;
		});
	}

	public static RedissonClient redisson() {
		return redissonClients.get();
	}

	public static Future<MigrateResult> migrate(String url, String username, String password) {
		return executeBlocking(() -> {
			var flyway = Flyway.configure()
					.group(true)
					.mixed(true)
					.schemas("hiddenswitch")
					.locations("classpath:db/migration", "classpath:com/hiddenswitch/framework/migrations")
					.dataSource(url, username, password)
					.load();
			flyway.repair();
			return flyway.migrate();
		});
	}

	public static Future<MigrateResult> migrate(ServerConfiguration serverConfiguration) {
		var pg = serverConfiguration.getPg();
		return migrate("jdbc:postgresql://" + pg.getHost() + ":" + pg.getPort() + "/" + pg.getDatabase(), pg.getUser(), pg.getPassword());
	}

	public static Future<Integer> migrate() {
		return Environment.migrate(getConfiguration()).map(v -> v.migrationsExecuted);
	}

	public static VertxOptions vertxOptions() {
		var configuration = getConfiguration();
		TracingOptions tracingOptions = null;
		ClusterManager clusterManager = null;
		if (configuration.hasJaeger() && configuration.getJaeger().getEnabled()) {
			tracingOptions = new OpenTracingOptions(Tracing.tracing());
		}
		if (configuration.hasVertx() && configuration.getVertx().getUseInfinispanClusterManager()) {
			var port = configuration.getVertx().getInfinspanPort();
			var isKubernetes = System.getenv().containsKey("KUBERNETES_SERVICE_HOST");
			clusterManager = new InfinispanClusterManager(isKubernetes ? Clustered.infinispanClusterManagerKubernetes(port) : Clustered.infinispanClusterManagerUdp(port));
		}
		return new VertxOptions()
				.setEventLoopPoolSize(Math.max(CpuCoreSensor.availableProcessors() * 2, 8))
				.setInternalBlockingPoolSize(Math.max(CpuCoreSensor.availableProcessors() * 4, 16))
				.setTracingOptions(tracingOptions)
				.setClusterManager(clusterManager)
				.setMetricsOptions(
						new MicrometerMetricsOptions()
								.setMicrometerRegistry(Metrics.globalRegistry)
								.setEnabled(true));
	}

	public static <T> Future<T> executeBlocking(Callable<T> blockingCallable) {
		return executeBlocking(Vertx.currentContext(), blockingCallable);
	}

	public static <T> Future<T> executeBlocking(Vertx vertx, Callable<T> blockingCallable) {
		return executeBlocking(vertx.getOrCreateContext(), blockingCallable);
	}

	public static <T> Future<T> timeout(Future<T> future, long milliseconds) {
		return timeout(future, milliseconds, null);
	}

	public static <T> Future<T> timeout(Future<T> future, long milliseconds, @Nullable Vertx vertx) {
		var here = new Throwable();
		var promise = Promise.<T>promise();
		Environment.sleep(vertx, milliseconds)
				.onComplete(v -> {
					if (!promise.future().isComplete()) {
						var t = new TimeoutException();
						t.setStackTrace(Environment.concatAndFilterStackTrace(t, here));
						promise.fail(t);
					}
				});

		future.onComplete(res -> {
			if (res.succeeded()) {
				promise.tryComplete(res.result());
			}
			if (res.failed()) {
				promise.tryFail(res.cause());
			}
		});

		return promise.future();
	}

	public static <T> Future<T> executeBlocking(Context context, Callable<T> blockingCallable) {
		var result = Promise.<T>promise();
		if (context != null) {
			context.executeBlocking(promise -> {
				try {
					var res = blockingCallable.call();
					promise.complete(res);
				} catch (Throwable e) {
					promise.fail(e);
				}
			}, false, result);
		} else {
			try {
				result.complete(blockingCallable.call());
			} catch (Throwable t) {
				result.fail(t);
			}
		}
		return result.future();
	}

	public static ServerConfiguration getConfiguration() {
		return cachedConfiguration.updateAndGet(existing -> {
			if (existing != null) {
				return existing;
			}

			var defaultConfiguration = defaultConfiguration();
			var filesConfiguration = fileConfigurations();
			// todo: pods have weird environment variables, must test this more
			// var environmentConfiguration = environmentConfiguration();
			var configuration = Streams.concat(
					Stream.of(defaultConfiguration),
					filesConfiguration,
					// Stream.of(environmentConfiguration),
					Stream.of(programmaticConfiguration)
			).reduce((s1, s2) -> s1.toBuilder().mergeFrom(s2).build());
			return configuration.orElseThrow();
		});
	}

	public static void setConfiguration(ServerConfiguration configuration) {
		cachedConfiguration.set(null);
		programmaticConfiguration = configuration;
	}

	@NotNull
	public static ServerConfiguration environmentConfiguration() {
		return ModelConversions.fromStringMap(ServerConfiguration.getDefaultInstance(), CONFIGURATION_NAME_TOKEN.toLowerCase(), "_", System.getenv());
	}

	public static Stream<ServerConfiguration> fileConfigurations() {
		var currentWorkingDirectoryPath = FileSystems.getDefault().getPath("conf");
		Stream<Path> filesystemConfigurationIterable = null;
		try {
			filesystemConfigurationIterable = Files.walk(currentWorkingDirectoryPath, 1, FileVisitOption.FOLLOW_LINKS);
		} catch (IOException e) {
			filesystemConfigurationIterable = Stream.empty();
		}
		var pattern = Pattern.compile("^.*" + CONFIGURATION_NAME_TOKEN + ".*\\.ya?ml$");
		var filesConfiguration = filesystemConfigurationIterable
				.map(Path::toFile)
				.filter(f -> pattern.asPredicate().test(f.getName().toLowerCase(Locale.ROOT)))
				.map(f -> {
					try {
						return Serialization.yamlMapper().readValue(f, ServerConfiguration.class);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		return filesConfiguration;
	}

	public static ServerConfiguration defaultConfiguration() {
		return ServerConfiguration.newBuilder()
				.setPg(ServerConfiguration.PostgresConfiguration.newBuilder()
						.setHost("localhost")
						.setDatabase("spellsource")
						.setUser("postgres")
						.setPassword("password")
						.build())
				.setKeycloak(ServerConfiguration.KeycloakConfiguration.newBuilder()
						.setAuthUrl("http://localhost:9090/")
						.setPublicAuthUrl("http://localhost:9090/")
						.setAdminUsername("admin")
						.setAdminPassword("password")
						.setRealmId("hiddenswitch")
						.setClientSecret("secret")
						.setClientId("clientid")
						.build())
				.setRedis(ServerConfiguration.RedisConfiguration.newBuilder()
						.setUri("redis://localhost:6379")
						.build())
				.setGrpcConfiguration(ServerConfiguration.GrpcConfiguration.newBuilder()
						.setPort(8081)
						.setServerKeepAliveTimeMillis(400)
						.setServerKeepAliveTimeoutMillis(8000)
						.setServerPermitKeepAliveWithoutCalls(true)
						.build())
				.setMatchmaking(ServerConfiguration.MatchmakingConfiguration.newBuilder()
						.setEnqueueLockTimeoutMillis(200)
						.setScanFrequencyMillis(2000)
						.setMaxTicketsToProcess(100)
						.build())
				.setDecks(ServerConfiguration.DecksConfiguration.newBuilder()
						.setCachedDeckTimeToLiveMinutes(60)
						.build())
				.setApplication(ServerConfiguration.ApplicationConfiguration.newBuilder()
						.setUseBroadcaster(false)
						.build())
				.setMigration(ServerConfiguration.MigrationConfiguration.newBuilder()
						.setShouldMigrate(false)
						.build())
				.setMetrics(ServerConfiguration.MetricsConfiguration.newBuilder()
						.setPort(8080)
						.setLivenessRoute("/liveness")
						.setReadinessRoute("/readiness")
						.setMetricsRoute("/metrics")
						.build())
				.setRateLimiter(ServerConfiguration.RateLimiterConfiguration.newBuilder()
						.setEnabled(false)
						.build())
				.setJaeger(ServerConfiguration.JaegerConfiguration.newBuilder()
						.setEnabled(false)
						.build())
				.setVertx(ServerConfiguration.VertxConfiguration.newBuilder()
						.setUseInfinispanClusterManager(false)
						.setInfinspanPort(7800)
						.build())
				.build();
	}

	/**
	 * Heuristically retrieves the primary networking interface for this device.
	 *
	 * @return A Java {@link NetworkInterface} object that can be used by {@link io.vertx.core.Vertx}.
	 */
	public static NetworkInterface mainInterface() {
		ArrayList<NetworkInterface> interfaces;
		try {
			interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return interfaces.stream().filter(ni -> {
			var isLoopback = false;
			var supportsMulticast = false;
			var isVirtualbox = false;
			var isSelfAssigned = false;
			var isHyperV = false;
			try {
				isSelfAssigned = ni.inetAddresses().anyMatch(i -> i.getHostAddress().startsWith("169"));
				isLoopback = ni.isLoopback();
				supportsMulticast = ni.supportsMulticast();
				isVirtualbox = ni.getDisplayName().contains("VirtualBox") || ni.getDisplayName().contains("Host-Only");
				isHyperV = ni.getDisplayName().contains("Hyper-V");
			} catch (IOException failure) {
			}
			var hasIPv4 = ni.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
			return supportsMulticast && !isSelfAssigned && !isLoopback && !ni.isVirtual() && hasIPv4 && !isVirtualbox && !isHyperV;
		}).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);
	}

	/**
	 * Retrieves a local-network-accessible IPv4 address for this instance by heuristically picking the "primary" network
	 * interface on this device.
	 *
	 * @return A string in the form of "192.168.0.1"
	 */
	@NotNull
	public static String getHostIpAddress() {
		try {
			var hostAddress = mainInterface().getInterfaceAddresses().stream().filter(ia -> ia.getAddress() instanceof Inet4Address).findFirst().orElse(null);
			if (hostAddress == null) {
				return "127.0.0.1";
			}
			return hostAddress.getAddress().getHostAddress();
		} catch (Throwable ex) {
			throw new VertxException(ex);
		}
	}

	public static <T> Function<Throwable, Future<T>> onGrpcFailure() {
		return t -> {
			if (t instanceof StatusRuntimeException) {
				return Future.failedFuture(t);
			}

			return Future.failedFuture(Status.INTERNAL
					.augmentDescription(Throwables.getRootCause(t).getMessage() + "\n" + Throwables.getStackTraceAsString(Throwables.getRootCause(t)))
					.withCause(t)
					.asRuntimeException());
		};
	}

	public static <T extends UpdatableRecord<T>> Insert<T> upsert(final DSLContext dslContext, final UpdatableRecord<T> record) {
		return dslContext.insertInto(record.getTable())
				.set(record)
				.onDuplicateKeyUpdate()
				.set(record);
	}

	static <T> RMapCacheAsync<String, T> cache(String name, Parser<T> parser) {
		return redisson().getMapCache(name, new CompositeCodec(new StringCodec(), new RedissonProtobufCodec(parser)));
	}

	public static PrometheusBackendRegistry registry() {
		return prometheusRegistry;
	}

	private record PgArgs(PgConnectOptions connectionOptions, PoolOptions poolOptions) {
	}

}
