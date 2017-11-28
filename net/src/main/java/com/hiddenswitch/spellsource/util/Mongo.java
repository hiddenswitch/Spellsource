package com.hiddenswitch.spellsource.util;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.util.MongoRecord;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.*;

import static com.hiddenswitch.spellsource.util.QuickJson.fromJson;
import static io.vertx.ext.sync.Sync.awaitResult;

import java.util.List;
import java.util.stream.Collectors;

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
				localMongoServer.stop();
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
		return connect(vertx, "mongodb://localhost:27017/production");
	}

	/**
	 * Connect by interpreting the MONGO_URL environment variable or the mongo.url system property. Otherwise, start an
	 * embedded server if it doens't already exist and connect to it.
	 *
	 * @param vertx The vertx instance to build the mongo client with.
	 * @return This {@link Mongo} instance.
	 */
	public Mongo connectWithEnvironment(Vertx vertx) {
		if (client != null) {
			return this;
		}

		if (System.getProperties().containsKey("mongo.url")
				|| System.getenv("MONGO_URL") != null) {
			String mongoUrl = System.getProperties().getProperty("mongo.url", System.getenv("MONGO_URL"));
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
	public <T extends MongoRecord> List<T> find(String collection, JsonObject query, Class<T> returnClass) {
		final List<JsonObject> objs = awaitResult(h -> client.find(collection, query, h));
		return QuickJson.fromJson(objs, returnClass);
	}

	@Suspendable
	public <T extends MongoRecord> List<T> findWithOptions(String collection, JsonObject query, FindOptions options, Class<T> returnClass) {
		final List<JsonObject> objs = awaitResult(h -> client.findWithOptions(collection, query, options, h));
		return QuickJson.fromJson(objs, returnClass);
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
		return QuickJson.fromJson(obj, returnClass);
	}

	@Suspendable
	public <T extends MongoRecord> T findOne(String collection, JsonObject query, Class<? extends T> returnClass) {
		final JsonObject obj = awaitResult(h -> client.findOne(collection, query, null, then -> h.handle(then.otherwiseEmpty())));
		return QuickJson.fromJson(obj, returnClass);
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

	/**
	 * Find a single matching document in the specified collection and update it.
	 * <p>
	 * This operation might change <i>_id</i> field of <i>query</i> parameter
	 *
	 * @param collection the collection
	 * @param query      the query used to match the document
	 * @param update     used to describe how the documents will be updated
	 */
	@Suspendable
	public <T extends MongoRecord> T findOneAndUpdate(String collection, JsonObject query, JsonObject update, Class<? extends T> returnClass) {
		final JsonObject obj = awaitResult(h -> client.findOneAndUpdate(collection, query, update, then -> h.handle(then.otherwiseEmpty())));
		return QuickJson.fromJson(obj, returnClass);
	}


	/**
	 * Execute a bulk operation. Can insert, update, replace, and/or delete multiple documents with one request.
	 *
	 * @param collection the collection
	 * @param operations the operations to execute
	 */
	@Suspendable
	public MongoClientBulkWriteResult bulkWrite(String collection, List<BulkOperation> operations) {
		return awaitResult(h -> client.bulkWrite(collection, operations, h));
	}

	/**
	 * Execute a bulk operation with the specified write options. Can insert, update, replace, and/or delete multiple
	 * documents with one request.
	 *
	 * @param collection       the collection
	 * @param operations       the operations to execute
	 * @param bulkWriteOptions the write options
	 */
	@Suspendable
	public MongoClientBulkWriteResult bulkWriteWithOptions(String collection, List<BulkOperation> operations, BulkWriteOptions bulkWriteOptions) {
		return awaitResult(h -> client.bulkWriteWithOptions(collection, operations, bulkWriteOptions, h));
	}

	/**
	 * Executes a bulk insert operation.
	 *
	 * @param collection The collection
	 * @param documents  The documents
	 * @return The result of the insert.
	 */
	@Suspendable
	public MongoClientBulkWriteResult insertMany(String collection, List<JsonObject> documents) {
		return bulkWrite(collection, documents.stream().map(BulkOperation::createInsert).collect(Collectors.toList()));
	}

	/**
	 * Executes a bulk insert operation with options.
	 *
	 * @param collection The collection
	 * @param documents  The documents
	 * @param options    The options for writing, specifically whether or not the insert has to be ordered.
	 * @return The result of the insert
	 */
	@Suspendable
	public MongoClientBulkWriteResult insertManyWithOptions(String collection, List<JsonObject> documents, BulkWriteOptions options) {
		return bulkWriteWithOptions(collection, documents.stream().map(BulkOperation::createInsert).collect(Collectors.toList()), options);
	}


	public void close() {
		if (client == null) {
			return;
		}
		client.close();
		client = null;
		instance = null;
	}
}
