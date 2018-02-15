package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sync.SyncVerticle;

/**
 * An abstract class providing common backend features for microservices in Spellsource.
 * <p>
 * Common features include a mongo client and configuration options for embedded or remote mongo connections. Some
 * logging is provided here too.
 * <p>
 * In the future, this class should provide access to non-Verticle services, like possibly things on AWS. It should
 * provide a way for a subclassing service to specify its needs and ensure they're met.
 *
 * @param <T>
 */
public abstract class AbstractService<T extends AbstractService<T>> extends SyncVerticle {
	protected static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractService.class);

	/**
	 * The entry point for the service. Should be overridden.
	 *
	 * @throws SuspendExecution
	 */
	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		// Sets up a mongo connection from the environment if it doesn't already exist
		Mongo.mongo().connectWithEnvironment(vertx);
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
		return new LocalRpcClient<>(thisClass, service);
	}
}
