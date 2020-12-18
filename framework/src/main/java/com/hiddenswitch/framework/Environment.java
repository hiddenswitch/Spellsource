package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.protobuf.GeneratedMessageV3;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.sync.Sync;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.sync.Sync.await;

public class Environment {

	static {
		// An opportunity to configure Vertx's JSON
		DatabindCodec.mapper().registerModule(new ProtobufModule())
				.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
	}

	private static AtomicReference<Configuration> jooqConfiguration = new AtomicReference<>();
	private static final WeakVertxMap<PgPool> pools = new WeakVertxMap<>(Environment::poolConstructor);
	private static final WeakVertxMap<ConfigRetriever> configRetrievers = new WeakVertxMap<>(Environment::configRetrieverConstructor);
	private static final WeakVertxMap<ReactiveClassicGenericQueryExecutor> queryExecutors = new WeakVertxMap<>(Environment::queryExecutorConstructor);
	private static ServerConfiguration cachedConfiguration;
	private static final JsonObject configurationOverride = new JsonObject();

	private static ReactiveClassicGenericQueryExecutor queryExecutorConstructor(Vertx vertx) {
		return new ReactiveClassicGenericQueryExecutor(jooqAkaDaoConfiguration(), sqlPoolAkaDaoDelegate());
	}

	private static PgPool poolConstructor(Vertx vertx) {
		var connectionOptions = connectOptions();
		var poolOptions = new PoolOptions()
				.setMaxSize(Runtime.getRuntime().availableProcessors() * 2);
		if (vertx == null) {
			return PgPool.pool(connectionOptions, poolOptions);
		}
		return PgPool.pool(vertx, connectionOptions, poolOptions);
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
			t.printStackTrace();
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
		var fiber = Fiber.currentFiber();
		if (context != null) {
			context.executeBlocking(promise -> {
				try {
					var res = blockingCallable.run();
					promise.complete(res);
				} catch (Throwable e) {
					promise.fail(e);
				}
			}, false, result);
			if (fiber != null) {
				try {
					T out = Sync.await(h -> result.future().onComplete(h));
					return Future.succeededFuture(out);
				} catch (Throwable t) {
					return Future.failedFuture(t);
				}
			}
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
		return DatabindCodec.mapper().convertValue(obj, targetClass);
	}

	/**
	 * Heuristically retrieves the primary networking interface for this device.
	 *
	 * @return A Java {@link NetworkInterface} object that can be used by {@link io.vertx.core.Vertx}.
	 */
	public static NetworkInterface mainInterface() {
		final ArrayList<NetworkInterface> interfaces;
		try {
			interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		final NetworkInterface networkInterface = interfaces.stream().filter(ni -> {
			boolean isLoopback = false;
			boolean supportsMulticast = false;
			boolean isVirtualbox = false;
			boolean isSelfAssigned = false;
			try {
				isSelfAssigned = ni.inetAddresses().anyMatch(i -> i.getHostAddress().startsWith("169"));
				isLoopback = ni.isLoopback();
				supportsMulticast = ni.supportsMulticast();
				isVirtualbox = ni.getDisplayName().contains("VirtualBox") || ni.getDisplayName().contains("Host-Only");
			} catch (IOException failure) {
			}
			final boolean hasIPv4 = ni.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
			return supportsMulticast && !isSelfAssigned && !isLoopback && !ni.isVirtual() && hasIPv4 && !isVirtualbox;
		}).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);
		return networkInterface;
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
			final InterfaceAddress hostAddress = mainInterface().getInterfaceAddresses().stream().filter(ia -> ia.getAddress() instanceof Inet4Address).findFirst().orElse(null);
			if (hostAddress == null) {
				return "127.0.0.1";
			}
			return hostAddress.getAddress().getHostAddress();
		} catch (Throwable ex) {
			throw new VertxException(ex);
		}
	}
}
