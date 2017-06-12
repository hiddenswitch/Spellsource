package com.hiddenswitch.proto3.net.impl;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.util.LocalMongo;
import com.hiddenswitch.proto3.net.util.Mongo;
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

	/**
	 * The entry point for the service. Should be overridden.
	 *
	 * @throws SuspendExecution
	 */
	@Override
	@Suspendable
	public void start() throws SuspendExecution {
	}


	/**
	 * Get a reference to the Mongo client.
	 *
	 * @return A Vertx MongoClient
	 */
	public MongoClient getMongo() {
		return Mongo.mongo().client();
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
