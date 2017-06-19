package com.hiddenswitch.proto3.net.util;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.util.MongoRecord;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.*;

import static com.hiddenswitch.proto3.net.util.QuickJson.fromJson;
import static io.vertx.ext.sync.Sync.awaitResult;

import java.util.List;

/**
 * There is only one Mongo.
 * <p>
 * Provide an easy way to access Mongo's methods in a sync pattern.
 */
public class Mongo {
	static Logger logger = LoggerFactory.getLogger(Mongo.class);
	static Mongo instance;
	MongoClient client;
	private LocalMongo localMongoServer;

	static {
		ch.qos.logback.classic.Logger mongoLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.ERROR);
	}

	public static Mongo mongo() {
		if (instance == null) {
			instance = new Mongo();
		}

		return instance;
	}

	public Mongo startEmbedded() {
		if (localMongoServer == null) {
			logger.info("Starting Mongod embedded...");
			localMongoServer = new LocalMongo();
			try {
				localMongoServer.start();
			} catch (Exception e) {
				logger.error("Mongo failed to start.", e);
				return this;
			}
			logger.info("Started Mongod embedded.");
		} else {
			logger.info("Mongod already started.");
		}
		return this;
	}

	public Mongo stopEmbedded() {
		if (localMongoServer != null) {
			try {
				client.close();
				localMongoServer.stop();
				client = null;
				localMongoServer = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}

	public Mongo connect(Vertx vertx, String connectionString) {
		if (client != null) {
			return this;
		}

		client = MongoClient.createShared(vertx, new JsonObject().put("connection_string", connectionString));
		return this;
	}

	public Mongo connect(Vertx vertx) {
		// Gets the connection string from the static field.
		return connect(vertx, "mongodb://localhost:27017");
	}

	/**
	 * Connect by interpreting the MONGO_URL environment variable or the mongo.url system property. Otherwise, start
	 * an embedded server if it doens't already exist and connect to it.
	 *
	 * @param vertx The vertx instance to build the mongo client with.
	 * @return This {@link Mongo} instance.
	 */
	public Mongo connectWithEnvironment(Vertx vertx) {
		if (client != null) {
			return this;
		}

		if (System.getProperties().containsKey("mongo.url")
				|| System.getenv().containsKey("MONGO_URL")) {
			String mongoUrl = System.getProperties().getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", "mongodb://localhost:27017/local"));
			return connect(vertx, mongoUrl);
		} else {
			return startEmbedded().connect(vertx);
		}
	}

	public MongoClient client() {
		return client;
	}

	@Suspendable
	public String insert(String collection, JsonObject document) {
		return awaitResult(h -> client.insert(collection, document, h));
	}

	@Suspendable
	public String insertWithOptions(String collection, JsonObject document, WriteOption writeOption) {
		return awaitResult(h -> client.insertWithOptions(collection, document, writeOption, h));
	}

	@Suspendable
	public MongoClientUpdateResult updateCollection(String collection, JsonObject query, JsonObject update) {
		return awaitResult(h -> client.updateCollection(collection, query, update, h));
	}

	@Suspendable
	public MongoClientUpdateResult updateCollectionWithOptions(String collection, JsonObject query, JsonObject update, UpdateOptions options) {
		return awaitResult(h -> client.updateCollectionWithOptions(collection, query, update, options, h));
	}

	@Suspendable
	public List<JsonObject> find(String collection, JsonObject query) {
		return awaitResult(h -> client.find(collection, query, h));
	}

	@Suspendable
	public List<JsonObject> findWithOptions(String collection, JsonObject query, FindOptions options) {
		return awaitResult(h -> client.findWithOptions(collection, query, options, then -> h.handle(then.otherwiseEmpty())));
	}

	/**
	 * @param collection
	 * @param query
	 * @param fields
	 * @return
	 */
	@Suspendable
	public JsonObject findOne(String collection, JsonObject query, JsonObject fields) {
		return awaitResult(h -> client.findOne(collection, query, fields, then -> h.handle(then.otherwiseEmpty())));
	}

	@Suspendable
	public <T extends MongoRecord> T findOne(String collection, JsonObject query, JsonObject fields, Class<? extends T> returnClass) {
		final JsonObject obj = awaitResult(h -> client.findOne(collection, query, fields, then -> h.handle(then.otherwiseEmpty())));
		return fromJson(obj, returnClass);
	}

	@Suspendable
	public <T extends MongoRecord> T findOne(String collection, JsonObject query, Class<? extends T> returnClass) {
		final JsonObject obj = awaitResult(h -> client.findOne(collection, query, null, then -> h.handle(then.otherwiseEmpty())));
		return fromJson(obj, returnClass);
	}

	@Suspendable
	public Long count(String collection, JsonObject query) {
		return awaitResult(h -> client.count(collection, query, h));
	}

	@Suspendable
	public List<String> getCollections() {
		return awaitResult(h -> client.getCollections(h));
	}

	@Suspendable
	public Void createIndex(String collection, JsonObject key) {
		return awaitResult(h -> client.createIndex(collection, key, h));
	}

	@Suspendable
	public Void createIndexWithOptions(String collection, JsonObject key, IndexOptions options) {
		return awaitResult(h -> client.createIndexWithOptions(collection, key, options, h));
	}

	@Suspendable
	public Void dropIndex(String collection, String indexName) {
		return awaitResult(h -> client.dropIndex(collection, indexName, h));
	}

	/**
	 * Remove matching documents from a collection and return the handler with MongoClientDeleteResult result
	 *
	 * @param collection the collection
	 * @param query      query used to match documents
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocuments(String collection, JsonObject query) {
		return awaitResult(h -> client.removeDocuments(collection, query, h));
	}

	/**
	 * Remove matching documents from a collection with the specified write option and return the handler with
	 * MongoClientDeleteResult result
	 *
	 * @param collection  the collection
	 * @param query       query used to match documents
	 * @param writeOption the write option to use
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocumentsWithOptions(String collection, JsonObject query, WriteOption writeOption) {
		return awaitResult(h -> client.removeDocumentsWithOptions(collection, query, writeOption, h));
	}

	/**
	 * Remove a single matching document from a collection and return the handler with MongoClientDeleteResult result
	 *
	 * @param collection the collection
	 * @param query      query used to match document
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocument(String collection, JsonObject query) {
		return awaitResult(h -> client.removeDocument(collection, query, h));
	}

	/**
	 * Remove a single matching document from a collection with the specified write option and return the handler with
	 * MongoClientDeleteResult result
	 *
	 * @param collection  the collection
	 * @param query       query used to match document
	 * @param writeOption the write option to use
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocumentWithOptions(String collection, JsonObject query, WriteOption writeOption) {
		return awaitResult(h -> client.removeDocumentWithOptions(collection, query, writeOption, h));
	}

	/**
	 * Create a new collection
	 *
	 * @param collectionName the name of the collection
	 */
	@Suspendable
	public Void createCollection(String collectionName) {
		return awaitResult(h -> client.createCollection(collectionName, h));
	}

	@Suspendable
	public <T extends MongoRecord> T findOneAndUpdate(String collection, JsonObject query, JsonObject update, Class<? extends T> returnClass) {
		final JsonObject obj = awaitResult(h -> client.findOneAndUpdate(collection, query, update, then -> h.handle(then.otherwiseEmpty())));
		return fromJson(obj, returnClass);
	}
}
