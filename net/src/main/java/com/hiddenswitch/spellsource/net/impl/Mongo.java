package com.hiddenswitch.spellsource.net.impl;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.util.MongoRecord;
import com.mongodb.MongoCommandException;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import org.apache.commons.lang3.RandomStringUtils;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.streamAdaptor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * There is only one Mongo.
 * <p>
 * Provide an easy way to access Mongo's methods in a sync pattern.
 */
public class Mongo {
	private static Mongo instance;
	private static final Object probe = new Object();
	private Map<Integer, MongoClient> clients = new ConcurrentHashMap<>();

	static {
		ch.qos.logback.classic.Logger mongoLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.ERROR);
	}

	private static Span getSpan(String operationName, JsonObject command) {
		Tracer tracer = GlobalTracer.get();
		String commandString = command.toString();
		return tracer.buildSpan("Mongo/" + operationName)
				.withTag(Tags.DB_TYPE, "mongo")
				.withTag(Tags.DB_STATEMENT, commandString.substring(0, Math.min(100, commandString.length())))
				.start();
	}

	public static Mongo mongo() {
		synchronized (probe) {
			if (instance == null) {
				instance = new Mongo();
			}
		}

		if (Vertx.currentContext() != null) {
			Vertx owner = Vertx.currentContext().owner();
			instance.getOrCreateClient(owner);
		} else {
			throw new RuntimeException("not in context");
		}

		return instance;
	}

	protected MongoClient getOrCreateClient(Vertx vertx, String connectionString) {
		return clients.computeIfAbsent(vertx.hashCode(), (k) -> {
			MongoClient client = MongoClient.createShared(vertx, new JsonObject().put("connection_string", connectionString));
			((VertxInternal) vertx).addCloseHook(v -> {
				clients.remove(vertx.hashCode());
				client.close();
				v.handle(Future.succeededFuture());
			});
			return client;
		});
	}

	protected MongoClient getOrCreateClient(Vertx vertx) {
		// Gets the connection string from the static field.
		String connectionString = System.getProperties().getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", "mongodb://localhost:27017/metastone"));
		return getOrCreateClient(vertx, connectionString);
	}

	@Suspendable
	public String insert(String collection, JsonObject document) {
		Span span = getSpan("insert", json("collection", collection, "document", document));
		try {
			return awaitResult(h -> client().insert(collection, document, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public String insertWithOptions(String collection, JsonObject document, WriteOption writeOption) {
		Span span = getSpan("insertWithOptions", json("document", document, "writeOption", writeOption));
		try {
			return awaitResult(h -> client().insertWithOptions(collection, document, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public MongoClientUpdateResult updateCollection(String collection, JsonObject query, JsonObject update) {
		Span span = getSpan("updateCollection", json("collection", collection, "query", query, "update", update));
		try {
			return awaitResult(h -> client().updateCollection(collection, query, update, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public MongoClientUpdateResult updateCollectionWithOptions(String collection, JsonObject query, JsonObject update, UpdateOptions options) {
		Span span = getSpan("updateCollectionWithOptions", json("collection", collection, "query", query, "update", update, "options", options.toJson()));
		try {
			if (options.isUpsert()
					&& update.containsKey("$set")
					&& update.getJsonObject("$set").containsKey("_id")) {
				// Fix for Mongo 3.6
				String id = (String) update.getJsonObject("$set").remove("_id");
				update.put("$setOnInsert", new JsonObject().put("_id", id));
			}
			return awaitResult(h -> client().updateCollectionWithOptions(collection, query, update, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public List<JsonObject> find(String collection, JsonObject query) {
		Span span = getSpan("find", json("collection", collection, "query", query));
		try {
			return awaitResult(h -> client().find(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public <T> List<T> find(String collection, JsonObject query, Class<T> returnClass) {
		Span span = getSpan("find", json("collection", collection, "query", query));
		try {
			final List<JsonObject> objs = awaitResult(h -> client().find(collection, query, h));
			return fromJson(objs, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public <T> List<T> findWithOptions(String collection, JsonObject query, FindOptions options, Class<T> returnClass) {
		Span span = getSpan("findWithOptions", json("collection", collection, "query", query, "options", options.toJson()));
		try {
			final List<JsonObject> objs = awaitResult(h -> client().findWithOptions(collection, query, options, h));
			return fromJson(objs, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public List<JsonObject> findWithOptions(String collection, JsonObject query, FindOptions options) {
		Span span = getSpan("findWithOptions", json("collection", collection, "query", query, "options", options.toJson()));
		try {
			return awaitResult(h -> client().findWithOptions(collection, query, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	/**
	 * @param collection
	 * @param query
	 * @param fields
	 * @return
	 */
	@Suspendable
	public JsonObject findOne(String collection, JsonObject query, JsonObject fields) {
		Span span = getSpan("findOne", json("collection", collection, "query", query, "fields", fields));
		try {
			return awaitResult(h -> client().findOne(collection, query, fields, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public <T> T findOne(String collection, JsonObject query, JsonObject fields, Class<? extends T> returnClass) {
		Span span = getSpan("findOne", json("collection", collection, "query", query, "fields", fields));
		try {
			JsonObject obj = awaitResult(h -> client().findOne(collection, query, fields, h));
			if (obj == null) {
				return null;
			}
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public <T> T findOne(String collection, JsonObject query, Class<? extends T> returnClass) {
		Span span = getSpan("findOne", json("collection", collection, "query", query));
		try {
			JsonObject obj = awaitResult(h -> client().findOne(collection, query, null, h));
			if (obj == null) {
				return null;
			}
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public Long count(String collection, JsonObject query) {
		Span span = getSpan("count", json("collection", collection, "query", query));
		try {
			return awaitResult(h -> client().count(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public List<String> getCollections() {
		Span span = getSpan("getCollections", new JsonObject());
		try {
			return awaitResult(h -> client().getCollections(h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public Void createIndex(String collection, JsonObject key) {
		Span span = getSpan("createIndex", json("collection", collection, "key", key));
		try {
			return awaitResult(h -> client().createIndex(collection, key, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public Void createIndexWithOptions(String collection, JsonObject key, IndexOptions options) {
		Span span = getSpan("createIndex", json("collection", collection, "key", key, "options", options.toJson()));
		try {
			return awaitResult(h -> client().createIndexWithOptions(collection, key, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	@Suspendable
	public Void dropIndex(String collection, String indexName) {
		Span span = getSpan("dropIndex", json("collection", collection, "indexName", indexName));
		try {
			return awaitResult(h -> client().dropIndex(collection, indexName, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	/**
	 * Remove matching documents from a collection and return the handler with MongoClientDeleteResult result
	 *
	 * @param collection the collection
	 * @param query      query used to match documents
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocuments(String collection, JsonObject query) {
		Span span = getSpan("removeDocuments", json("collection", collection, "query", query));
		try {
			return awaitResult(h -> client().removeDocuments(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
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
		Span span = getSpan("removeDocumentsWithOptions", json("collection", collection, "query", query, "writeOption", writeOption));
		try {
			return awaitResult(h -> client().removeDocumentsWithOptions(collection, query, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	/**
	 * Remove a single matching document from a collection and return the handler with MongoClientDeleteResult result
	 *
	 * @param collection the collection
	 * @param query      query used to match document
	 */
	@Suspendable
	public MongoClientDeleteResult removeDocument(String collection, JsonObject query) {
		Span span = getSpan("removeDocument", json("collection", collection, "query", query));
		try {
			return awaitResult(h -> client().removeDocument(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
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
		Span span = getSpan("removeDocumentWithOptions", json("collection", collection, "query", query, "writeOption", writeOption));
		try {
			return awaitResult(h -> client().removeDocumentWithOptions(collection, query, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}

	/**
	 * Create a new collection
	 *
	 * @param collectionName the name of the collection
	 */
	@Suspendable
	public Void createCollection(String collectionName) {
		Span span = getSpan("createCollection", json("collectionName", collectionName));
		try {
			return awaitResult(h -> client().createCollection(collectionName, h));
		} catch (MongoCommandException ex) {
			if (ex.getErrorCode() == 48 /*Namespace already exists*/) {
				return null;
			} else {
				throw ex;
			}
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
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
		Span span = getSpan("findOneAndUpdate", json("collection", collection, "query", query, "update", update));
		try {
			JsonObject obj = awaitResult(h -> client().findOneAndUpdate(collection, query, update, h));
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
	}


	/**
	 * Execute a bulk operation. Can insert, update, replace, and/or delete multiple documents with one request.
	 *
	 * @param collection the collection
	 * @param operations the operations to execute
	 */
	@Suspendable
	public MongoClientBulkWriteResult bulkWrite(String collection, List<BulkOperation> operations) {
		Span span = getSpan("bulkWrite", json("collection", collection, "count", operations.size()));
		try {
			MongoClientBulkWriteResult res = awaitResult(h -> client().bulkWrite(collection, operations, h));
			span.setTag("insertedCount", res.getInsertedCount())
					.setTag("matchedCount", res.getMatchedCount())
					.setTag("modifiedCount", res.getModifiedCount())
					.setTag("deletedCount", res.getDeletedCount());
			return res;
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
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
		Span span = getSpan("bulkWrite", json("collection", collection, "count", operations.size(), "bulkWriteOptions", bulkWriteOptions.toJson()));
		try {
			MongoClientBulkWriteResult res = awaitResult(h -> client().bulkWriteWithOptions(collection, operations, bulkWriteOptions, h));
			span.setTag("insertedCount", res.getInsertedCount())
					.setTag("matchedCount", res.getMatchedCount())
					.setTag("modifiedCount", res.getModifiedCount())
					.setTag("deletedCount", res.getDeletedCount());
			return res;
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			span.finish();
		}
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

	/*
	 * Subscribes to the change stream specified by the given aggregation pipeline.
	 * <p>
	 * {@code pipeline} describes a Mongo "aggregation" pipeline object limited to {@code $match}, {@code $project},
	 * {@code $addFields}, {@code $replaceRoot} and {@code $redact} commands. This pipeline is evaluated not against the
	 * underlying collection's document but against a "change event" document:
	 *
	 * <pre>
	 * {
	 *    _id : { (metadata related to the change) },
	 *    "operationType" : (one of {@link MongoClientChangeOperationType}),
	 *    "fullDocument" : { the collection document when {@link WatchOptions#fullDocument} is set to "updateLookup" },
	 *    "ns" : {
	 *       "db" : "database",
	 *       "coll" : "collection"
	 *    },
	 *    "documentKey" : { "_id" : (ObjectId or string) },
	 *    "updateDescription" : {
	 *       "updatedFields" : { key-value pairs of updated fields, whose keys match the fields as updated by an update
	 *                           cmd },
	 *       "removedFields" : [ "field names", ... ]
	 *    }
	 * }
	 * </pre>
	 * <p>
	 * This change document format differs slightly from {@link MongoClientChange} due to differences in the Mongo
	 * driver.
	 * <p>
	 * {@code options} and {@code pipeline} should not be {@code null}. Use {@code new JsonArray()} as your {@code
	 * pipeline} to specify all changes, and {@code new WatchOptions()} for default options.
	 * <p>
	 * Requires that the mongod server is running at least 3.6.0 and that the connection is a replica set connection
	 * (e.g., {@code "mongodb://localhost:27017/dbname?replicaSet=replSetName"}.
	 * <p>
	 * Trying to watch for change streams on a non-replica-set node will fail.
	 * <p>
	 * Each watch uses 1 connection to the database until {@link MongoClientChangeStream#close(Handler)} is called.
	 * Reportedly, there is a limit of <a href="https://github.com/meteor/meteor-feature-requests/issues/158#issuecomment-339376181">
	 * 1,000 change stream watches</a> in Mongo 3.6. This will exceed the default max connections in any case.
	 *
	 * @param collection   the collection
	 * @param pipeline     the Mongo aggregation pipeline
	 * @param watchOptions the options
	@Suspendable public MongoClientChangeStream<MongoClientChange> watch(String collection, JsonArray pipeline,
	                                                                     WatchOptions watchOptions) { return awaitResult(h -> client().watch(collection, pipeline, watchOptions, h)); }
	*/

	/**
	 * Gets the underlying client connection.
	 *
	 * @return
	 */
	public MongoClient client() {
		if (Vertx.currentContext() == null) {
			throw new NullPointerException("not on context");
		}
		Vertx owner = Vertx.currentContext().owner();
		return getOrCreateClient(owner);
	}

	/**
	 * Creates a mongo ID that is also suitable for use as a URL parameter.
	 *
	 * @return
	 */
	public String createId() {
		return RandomStringUtils.randomAlphanumeric(24).toLowerCase();
	}
}