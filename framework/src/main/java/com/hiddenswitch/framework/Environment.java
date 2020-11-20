package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.ServerConfiguration;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.sync.Sync;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.ext.sync.Sync.await;

public class Environment {

	static {
		// An opportunity to configure Vertx's JSON
		DatabindCodec.mapper().registerModule(new ProtobufModule());
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
		return queryExecutors.get();
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

	public static Future<Integer> migrate() {
		return configuration()
				.compose(serverConfiguration -> {
					var pg = serverConfiguration.getPg();
					return migrate("jdbc:postgresql://" + pg.getHost() + ":" + pg.getPort() + "/" + pg.getDatabase(), pg.getUser(), pg.getPassword());
				});
	}

	@Suspendable
	public static <T> Future<T> executeBlocking(SuspendableCallable<T> blockingCallable) {
		var context = Vertx.currentContext();
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
}
