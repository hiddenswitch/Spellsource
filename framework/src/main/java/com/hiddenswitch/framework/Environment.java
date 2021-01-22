package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.github.marschall.micrometer.jfr.JfrMeterRegistry;
import com.google.common.base.Throwables;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import com.hiddenswitch.framework.impl.RedissonProtobufCodec;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Context;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.ext.sync.Sync;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.PrometheusBackendRegistry;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DefaultConfiguration;
import org.redisson.Redisson;
import org.redisson.api.RMapCacheAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.config.Config;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.vertx.core.json.jackson.DatabindCodec.mapper;

public class Environment {
	private static final PrometheusBackendRegistry prometheusRegistry = new PrometheusBackendRegistry(new VertxPrometheusOptions()
			.setEnabled(true)
			.setStartEmbeddedServer(true)
			.setEmbeddedServerEndpoint("/metrics")
			.setPublishQuantiles(true)
			.setEmbeddedServerOptions(new HttpServerOptions().setPort(8080)));
	private static final AtomicBoolean prometheusInited = new AtomicBoolean();
	private static final AtomicReference<Configuration> jooqConfiguration = new AtomicReference<>();
	private static final WeakVertxMap<PgPool> pools = new WeakVertxMap<>(Environment::poolConstructor);
	private static final WeakVertxMap<RedissonClient> redissonClients = new WeakVertxMap<>(Environment::redissonClientConstructor);
	private static final WeakVertxMap<ConfigRetriever> configRetrievers = new WeakVertxMap<>(Environment::configRetrieverConstructor);
	private static ServerConfiguration cachedConfiguration;
	private static final JsonObject configurationOverride = new JsonObject();

	static {
		// An opportunity to configure Vertx's JSON
		mapper().registerModule(new ProtobufModule())
				.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
		// metrics
		metrics();
	}

	private static RedissonClient redissonClientConstructor(Vertx vertx) {
		var configuration = cachedConfigurationOrGet();
		var config = new Config();
		config.useSingleServer().setAddress(configuration.getRedis().getUri());
		return Redisson.create(config);
	}

	private static ReactiveClassicGenericQueryExecutor queryExecutorConstructor(Vertx vertx) {
		return new ReactiveClassicGenericQueryExecutor(jooqAkaDaoConfiguration(), sqlPoolAkaDaoDelegate());
	}

	private static PgPool poolConstructor(Vertx vertx) {
		var connectionOptions = connectOptions();
		var poolOptions = new PoolOptions()
				.setMaxSize(Runtime.getRuntime().availableProcessors());
		if (vertx == null) {
			return PgPool.pool(connectionOptions, poolOptions);
		}
		return PgPool.pool(vertx, connectionOptions, poolOptions);
	}

	public static void metrics() {
		if (prometheusInited.compareAndSet(false, true)) {
			Metrics.addRegistry(new JfrMeterRegistry());
			Metrics.addRegistry(prometheusRegistry.getMeterRegistry());
			prometheusRegistry.init();
		}
	}

	public synchronized static PgConnectOptions connectOptions() {
		var options = new PgConnectOptions();
		var cachedConfiguration = cachedConfigurationOrGet();
		if (!cachedConfiguration.hasPg()) {
			return options;
		}

		var pg = cachedConfiguration.getPg();
		if (pg.getPort() != 0) {
			options.setPort(pg.getPort());
		}
		if (!pg.getHost().isEmpty()) {
			options.setHost(pg.getHost());
		}
		if (!pg.getPassword().isEmpty()) {
			options.setPassword(pg.getPassword());

		}
		if (!pg.getDatabase().isEmpty()) {
			options.setDatabase(pg.getDatabase());
		}
		if (!pg.getUser().isEmpty()) {
			options.setUser(pg.getUser());
		}
		if (!pg.getPassword().isEmpty()) {
			options.setPassword(pg.getPassword());
		}
		return options;
	}

	public static PgPool sqlPoolAkaDaoDelegate() {
		return pools.get();
	}

	public static ReactiveClassicGenericQueryExecutor queryExecutor() {
		return queryExecutorConstructor(null);
	}

	public static Handler<Throwable> onFailure() {
		var here = new Throwable();
		return t -> {
			t.setStackTrace(Sync.concatAndFilterStackTrace(t, here));
			Tracing.error(t);
		};
	}

	public static Future<Void> sleep(Vertx vertx, long milliseconds) {
		var fut = Promise.<Long>promise();
		vertx.setTimer(milliseconds, fut::complete);
		return fut.future().mapEmpty();
	}

	public static Future<Void> sleep(long milliseconds) {
		return sleep(Vertx.currentContext().owner(), milliseconds);
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

	public static Future<Integer> migrate(String url, String username, String password) {
		return executeBlocking(() -> {
			var flyway = Flyway.configure()
					.schemas("hiddenswitch")
					.locations("classpath:db/migration", "classpath:com/hiddenswitch/framework/migrations")
					.dataSource(url, username, password)
					.load();
			return flyway.migrate();
		});
	}

	public static VertxOptions vertxOptions() {
		return new VertxOptions()
				.setTracingOptions(new TracingOptions())
				.setMetricsOptions(
						new MicrometerMetricsOptions()
								.setMicrometerRegistry(Metrics.globalRegistry)
								.setEnabled(true));
	}

	public static Future<Integer> migrate(ServerConfiguration serverConfiguration) {
		var pg = serverConfiguration.getPg();
		return migrate("jdbc:postgresql://" + pg.getHost() + ":" + pg.getPort() + "/" + pg.getDatabase(), pg.getUser(), pg.getPassword());
	}

	public static Future<Integer> migrate() {
		return configuration()
				.compose(Environment::migrate);
	}

	@Suspendable
	public static <T> Future<T> executeBlocking(SuspendableCallable<T> blockingCallable) {
		return executeBlocking(Vertx.currentContext(), blockingCallable);
	}

	@Suspendable
	public static <T> Future<T> executeBlocking(Vertx vertx, SuspendableCallable<T> blockingCallable) {
		return executeBlocking(vertx.getOrCreateContext(), blockingCallable);
	}

	@Suspendable
	public static <T> Future<T> executeBlocking(Context context, SuspendableCallable<T> blockingCallable) {
		var result = Promise.<T>promise();
		if (context != null) {
			context.executeBlocking(promise -> {
				try {
					var res = blockingCallable.run();
					promise.complete(res);
				} catch (Throwable e) {
					promise.fail(e);
				}
			}, false, result);
		} else {
			try {
				result.complete(blockingCallable.run());
			} catch (Throwable t) {
				result.fail(t);
			}
		}
		return result.future();
	}

	private static ConfigRetriever configRetriever() {
		return configRetrievers.get();
	}

	public static void setConfiguration(ServerConfiguration configuration) {
		configurationOverride.mergeIn(JsonObject.mapFrom(configuration), true);
		if (cachedConfiguration != null) {
			cachedConfiguration = ServerConfiguration.newBuilder(cachedConfiguration).mergeFrom(configuration).build();
		} else {
			cachedConfiguration = ServerConfiguration.newBuilder(configuration).build();
		}
	}

	public static Future<ServerConfiguration> configuration() {
		var promise = Promise.<JsonObject>promise();
		configRetriever().getConfig(promise);
		return promise.future()
				.compose(jsonObject -> Future.succeededFuture(jsonObject.mapTo(ServerConfiguration.class)))
				.onSuccess(s -> cachedConfiguration = s);
	}

	public synchronized static ServerConfiguration cachedConfigurationOrGet() {
		if (cachedConfiguration == null) {
			configuration()
					.toCompletionStage()
					.toCompletableFuture()
					.orTimeout(1900L, TimeUnit.MILLISECONDS)
					.join();
		}
		return cachedConfiguration;
	}

	private static <T> ConfigRetriever configRetrieverConstructor(Vertx vertx) {
		if (vertx == null) {
			vertx = Vertx.vertx();
		}
		return ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
				// TODO: Add more stores)
				/*.addStore(new ConfigStoreOptions()
						.setType("sys")
						.setConfig(new JsonObject().put("cache", false)))
				.addStore(new ConfigStoreOptions()
						.setType("env"))*/
				.addStore(new ConfigStoreOptions()
						.setType("json")
						.setConfig(configurationOverride))
		);
	}

	public static <T extends GeneratedMessageV3> T toProto(Object obj, Class<T> targetClass) {
		return mapper().convertValue(obj, targetClass);
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
			try {
				isSelfAssigned = ni.inetAddresses().anyMatch(i -> i.getHostAddress().startsWith("169"));
				isLoopback = ni.isLoopback();
				supportsMulticast = ni.supportsMulticast();
				isVirtualbox = ni.getDisplayName().contains("VirtualBox") || ni.getDisplayName().contains("Host-Only");
			} catch (IOException failure) {
			}
			var hasIPv4 = ni.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
			return supportsMulticast && !isSelfAssigned && !isLoopback && !ni.isVirtual() && hasIPv4 && !isVirtualbox;
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

	public static Vertx vertx() {
		return Vertx.vertx(vertxOptions());
	}
}
