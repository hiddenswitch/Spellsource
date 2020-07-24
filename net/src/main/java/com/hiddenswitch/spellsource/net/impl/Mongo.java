package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.util.MongoRecord;
import com.mongodb.MongoCommandException;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResultUninterruptibly;

/**
 * There is only one Mongo.
 * <p>
 * Provide an easy way to access Mongo's methods in a sync pattern.
 */
public class Mongo implements Closeable {
	public static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017/metastone";
	private final MongoClient client;
	private final AtomicInteger pending = new AtomicInteger();
	private final AtomicBoolean isClosing = new AtomicBoolean();
	private final Context createdOnContext;
	private Handler<AsyncResult<Void>> closeFut;

	protected Mongo(@NotNull MongoClient client, @NotNull Context context) {
		this.client = client;
		this.createdOnContext = context;
	}

	private Span getSpan(String operationName, JsonObject command) {
		var tracer = GlobalTracer.get();
		var commandString = command.toString();
		return tracer.buildSpan("Mongo/" + operationName)
				.withTag(Tags.DB_TYPE, "mongo")
				.withTag(Tags.DB_STATEMENT, commandString.substring(0, Math.min(1000, commandString.length())))
				.withTag("pending", pending.intValue())
				.start();
	}

	public synchronized static Mongo mongo() {
		@Nullable Context context = Vertx.currentContext();
		if (context == null) {
			throw new RuntimeException("not in context");
		}

		var client = context.<Mongo>get("__mongo");
		var connectionString = System.getProperties().getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", DEFAULT_CONNECTION_STRING));

		if (client == null) {
			client = new Mongo(MongoClient.create(context.owner(), new JsonObject().put("connection_string", connectionString)), context);
			context.put("__mongo", client);
			context.addCloseHook(client);
		}

		return client;
	}

	@Suspendable
	public String insert(String collection, JsonObject document) {
		var span = getSpan("insert", json("collection", collection, "document", document));
		mongoTry();
		try {
			return awaitResultUninterruptibly(h -> client().insert(collection, document, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	private void mongoTry() {
		pending.incrementAndGet();
	}

	private void mongoFinally(Span span) {
		span.finish();
		// If this was the last task, close in a short while, which will check again that pending task count is zero
		if (pending.decrementAndGet() == 0 && isClosing.get()) {
			closeShortly();
		}
	}

	@Suspendable
	public String insertWithOptions(String collection, JsonObject document, WriteOption writeOption) {
		mongoTry();
		var span = getSpan("insertWithOptions", json("document", document, "writeOption", writeOption));
		try {
			return awaitResultUninterruptibly(h -> client().insertWithOptions(collection, document, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public MongoClientUpdateResult updateCollection(String collection, JsonObject query, JsonObject update) {
		mongoTry();
		var span = getSpan("updateCollection", json("collection", collection, "query", query, "update", update));
		try {
			return awaitResultUninterruptibly(h -> client().updateCollection(collection, query, update, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public MongoClientUpdateResult updateCollectionWithOptions(String collection, JsonObject query, JsonObject update, UpdateOptions options) {
		mongoTry();
		var span = getSpan("updateCollectionWithOptions", json("collection", collection, "query", query, "update", update, "options", options.toJson()));
		try {
			if (options.isUpsert()
					&& update.containsKey("$set")
					&& update.getJsonObject("$set").containsKey("_id")) {
				// Fix for Mongo 3.6
				var id = (String) update.getJsonObject("$set").remove("_id");
				update.put("$setOnInsert", new JsonObject().put("_id", id));
			}
			return awaitResultUninterruptibly(h -> client().updateCollectionWithOptions(collection, query, update, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public List<JsonObject> find(String collection, JsonObject query) {
		mongoTry();
		var span = getSpan("find", json("collection", collection, "query", query));
		try {
			return awaitResultUninterruptibly(h -> client().find(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public <T> List<T> find(String collection, JsonObject query, Class<T> returnClass) {
		mongoTry();
		var span = getSpan("find", json("collection", collection, "query", query));
		try {
			final List<JsonObject> objs = awaitResultUninterruptibly(h -> client().find(collection, query, h));
			return fromJson(objs, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public <T> List<T> findWithOptions(String collection, JsonObject query, FindOptions options, Class<T> returnClass) {
		mongoTry();
		var span = getSpan("findWithOptions", json("collection", collection, "query", query, "options", options.toJson()));
		try {
			final List<JsonObject> objs = awaitResultUninterruptibly(h -> client().findWithOptions(collection, query, options, h));
			return fromJson(objs, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public List<JsonObject> findWithOptions(String collection, JsonObject query, FindOptions options) {
		mongoTry();
		var span = getSpan("findWithOptions", json("collection", collection, "query", query, "options", options.toJson()));
		try {
			return awaitResultUninterruptibly(h -> client().findWithOptions(collection, query, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("findOne", json("collection", collection, "query", query, "fields", fields));
		try {
			return awaitResultUninterruptibly(h -> client().findOne(collection, query, fields, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public <T> T findOne(String collection, JsonObject query, JsonObject fields, Class<? extends T> returnClass) {
		mongoTry();
		var span = getSpan("findOne", json("collection", collection, "query", query, "fields", fields));
		try {
			JsonObject obj = awaitResultUninterruptibly(h -> client().findOne(collection, query, fields, h));
			if (obj == null) {
				return null;
			}
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public <T> T findOne(String collection, JsonObject query, Class<? extends T> returnClass) {
		mongoTry();
		var span = getSpan("findOne", json("collection", collection, "query", query));
		try {
			JsonObject obj = awaitResultUninterruptibly(h -> client().findOne(collection, query, null, h));
			if (obj == null) {
				return null;
			}
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public Long count(String collection, JsonObject query) {
		mongoTry();
		var span = getSpan("count", json("collection", collection, "query", query));
		try {
			return awaitResultUninterruptibly(h -> client().count(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public List<String> getCollections() {
		mongoTry();
		var span = getSpan("getCollections", new JsonObject());
		try {
			return awaitResultUninterruptibly(h -> client().getCollections(h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public Void createIndex(String collection, JsonObject key) {
		mongoTry();
		var span = getSpan("createIndex", json("collection", collection, "key", key));
		try {
			return awaitResultUninterruptibly(h -> client().createIndex(collection, key, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public Void createIndexWithOptions(String collection, JsonObject key, IndexOptions options) {
		mongoTry();
		var span = getSpan("createIndex", json("collection", collection, "key", key, "options", options.toJson()));
		try {
			return awaitResultUninterruptibly(h -> client().createIndexWithOptions(collection, key, options, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	@Suspendable
	public Void dropIndex(String collection, String indexName) {
		mongoTry();
		var span = getSpan("dropIndex", json("collection", collection, "indexName", indexName));
		try {
			return awaitResultUninterruptibly(h -> client().dropIndex(collection, indexName, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("removeDocuments", json("collection", collection, "query", query));
		try {
			return awaitResultUninterruptibly(h -> client().removeDocuments(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("removeDocumentsWithOptions", json("collection", collection, "query", query, "writeOption", writeOption));
		try {
			return awaitResultUninterruptibly(h -> client().removeDocumentsWithOptions(collection, query, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("removeDocument", json("collection", collection, "query", query));
		try {
			return awaitResultUninterruptibly(h -> client().removeDocument(collection, query, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("removeDocumentWithOptions", json("collection", collection, "query", query, "writeOption", writeOption));
		try {
			return awaitResultUninterruptibly(h -> client().removeDocumentWithOptions(collection, query, writeOption, h));
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
		}
	}

	/**
	 * Create a new collection
	 *
	 * @param collectionName the name of the collection
	 */
	@Suspendable
	public Void createCollection(String collectionName) {
		mongoTry();
		var span = getSpan("createCollection", json("collectionName", collectionName));
		try {
			return awaitResultUninterruptibly(h -> client().createCollection(collectionName, h));
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
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("findOneAndUpdate", json("collection", collection, "query", query, "update", update));
		try {
			JsonObject obj = awaitResultUninterruptibly(h -> client().findOneAndUpdate(collection, query, update, h));
			return fromJson(obj, returnClass);
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("bulkWrite", json("collection", collection, "count", operations.size()));
		try {
			MongoClientBulkWriteResult res = awaitResultUninterruptibly(h -> client().bulkWrite(collection, operations, h));
			span.setTag("insertedCount", res.getInsertedCount())
					.setTag("matchedCount", res.getMatchedCount())
					.setTag("modifiedCount", res.getModifiedCount())
					.setTag("deletedCount", res.getDeletedCount());
			return res;
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
		mongoTry();
		var span = getSpan("bulkWrite", json("collection", collection, "count", operations.size(), "bulkWriteOptions", bulkWriteOptions.toJson()));
		try {
			MongoClientBulkWriteResult res = awaitResultUninterruptibly(h -> client().bulkWriteWithOptions(collection, operations, bulkWriteOptions, h));
			span.setTag("insertedCount", res.getInsertedCount())
					.setTag("matchedCount", res.getMatchedCount())
					.setTag("modifiedCount", res.getModifiedCount())
					.setTag("deletedCount", res.getDeletedCount());
			return res;
		} catch (Throwable throwable) {
			Tracing.error(throwable, span, true);
			throw throwable;
		} finally {
			mongoFinally(span);
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
	                                                                     WatchOptions watchOptions) { return awaitResultUninterruptibly(h -> client().watch(collection, pipeline, watchOptions, h)); }
	*/

	/**
	 * Gets the underlying client connection.
	 *
	 * @return
	 */
	public MongoClient client() {
		return client;
	}

	/**
	 * Creates a mongo ID that is also suitable for use as a URL parameter.
	 *
	 * @return
	 */
	public String createId() {
		return RandomStringUtils.randomAlphanumeric(24).toLowerCase();
	}

	@Override
	public void close(Handler<AsyncResult<Void>> completionHandler) {
		if (isClosing.compareAndSet(false, true)) {
			closeFut = completionHandler;
			createdOnContext.runOnContext(v -> {
				if (pending.get() == 0) {
					// Check if there's still nothing pending in a short while
					closeShortly();
				}
			});
		}
	}

	private void closeShortly() {
		createdOnContext.owner().setTimer(1000L, timerId -> {
			if (pending.get() == 0) {
				closeNow();
			}
		});
	}

	private void closeNow() {
		var closeFutThis = closeFut;
		if (closeFutThis == null) {
			return;
		}
		closeFut = null;
		try {
			client.close();
			createdOnContext.remove("__mongo");
			closeFutThis.handle(Future.succeededFuture());
		} catch (Throwable t) {
			closeFutThis.handle(Future.failedFuture(t));
		}
	}
}