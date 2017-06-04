package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.util.LocalMongo;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sync.SyncVerticle;

import java.io.File;
import java.lang.reflect.Proxy;

/**
 * An abstract class providing common backend features for microservices in Minionate.
 * <p>
 * Common features include a mongo client and configuration options for embedded or remote mongo connections. Some
 * logging is provided here too.
 * <p>
 * In the future, this class should provide access to non-Verticle services, like possibly things on AWS. It should
 * provide a way for a subclassing service to specify its needs and ensure they're met.
 *
 * @param <T>
 */
abstract class AbstractService<T extends AbstractService<T>> extends SyncVerticle {
	private static Logger logger = LoggerFactory.getLogger(AbstractService.class);
	private MongoClient mongo;
	private static boolean embeddedConfigured;
	private static LocalMongo localMongoServer;


	/**
	 * Configure the service to use an embedded runtime configuration. This means it will try to start all the
	 * non-Verticle services like the database locally, in its own process, instead of relying on external services.
	 * This is useful for testing.
	 *
	 * @param dbFile A path to use for the database.
	 * @return A reference to this instance.
	 */
	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration(File dbFile) {
		createdEmbeddedServices(dbFile);

		logger.info("Setting default services...");

		logger.info("Default services ready in embedded configuration.");
		return (T) this;
	}

	/**
	 * Configure the service to use an embedded runtime configuration. This means it will try to start all the
	 * non-Verticle services like the database locally, in its own process, instead of relying on external services.
	 * This is useful for testing. The default database file location is used.
	 *
	 * @return A reference to this instance.
	 */
	@SuppressWarnings("unchecked")
	public T withEmbeddedConfiguration() {
		return withEmbeddedConfiguration(null);
	}

	/**
	 * The entry point for the service. Should be overridden.
	 *
	 * @throws SuspendExecution
	 */
	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		if (this.mongo == null
				&& embeddedConfigured) {
			this.mongo = MongoClient.createShared(vertx, localMongoServer.getConfig());
		}
		ch.qos.logback.classic.Logger mongoLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(ch.qos.logback.classic.Level.ERROR);
	}

	/**
	 * A method that creates embedded services, i.e., starts a local copy of mongo and configures the inheriting class
	 * to use the local mongo as its mongo service.
	 *
	 * @param dbFile
	 */
	private synchronized static void createdEmbeddedServices(File dbFile) {
		if (localMongoServer == null) {
			logger.info("Starting Mongod embedded...");
			localMongoServer = new LocalMongo();
			try {
				localMongoServer.start();
			} catch (Exception e) {
				logger.error("Mongo failed to start.", e);
				return;
			}
			logger.info("Started Mongod embedded.");
		} else {
			logger.info("Mongod already started.");
		}

		if (embeddedConfigured) {
			return;
		}

		logger.info("Configuring stack...");
		logger.info("Stack initialized, embedding configured.");
		embeddedConfigured = true;
	}


	/**
	 * Get a reference to the Mongo client.
	 *
	 * @return A Vertx MongoClient
	 */
	public MongoClient getMongo() {
		return mongo;
	}

	/**
	 * Sets the Mongo client reference.
	 *
	 * @param mongo
	 */
	public void setMongo(MongoClient mongo) {
		this.mongo = mongo;
	}

	@SuppressWarnings("unchecked")
	public T withMongo(MongoClient client) {
		this.mongo = client;
		return (T) this;
	}

	/**
	 * Gets an {@link RpcClient} that makes direct calls to this instance, not calls over the network.
	 *
	 * @return An {@link RpcClient} that runs "locally."
	 */
	@SuppressWarnings("unchecked")
	public RpcClient<T> getLocalClient() {
		T service = (T) this;
		Class<?> thisClass = ((T) this).getClass();
		return new RpcClient<T>() {
			@Override
			@Suspendable
			public <R> T async(Handler<AsyncResult<R>> handler) {
				return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{thisClass}, (proxy, method, args) -> {
					try {
						Object result = method.invoke(service, args);
						handler.handle(Future.succeededFuture((R) result));
					} catch (Throwable e) {
						handler.handle(Future.failedFuture(e));
					}
					return null;
				});
			}

			@Override
			@Suspendable
			public T sync() throws SuspendExecution, InterruptedException {
				return service;
			}

			@Override
			@Suspendable
			public T uncheckedSync() {
				return service;
			}
		};
	}
}
