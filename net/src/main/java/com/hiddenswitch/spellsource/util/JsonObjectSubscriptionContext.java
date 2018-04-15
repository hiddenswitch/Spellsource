package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.UserId;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClientChange;
import io.vertx.ext.mongo.MongoClientChangeStream;

import java.util.List;

public interface JsonObjectSubscriptionContext {
	UserId user();

	JsonArray parameters();

	void added(String id, JsonObject object);

	void changed(String id, JsonObject updatedFields);

	void removed(String id);

	void delegate(JsonCursor cursor);

	void delegate(SuspendableMultimapCursor cursor);

	interface JsonCursor {
		ReadStream<JsonObject> addedDocuments();

		ReadStream<JsonObject> removedDocuments();

		ReadStream<JsonObject> changedFields();

		@Suspendable
		List<JsonObject> initial();
	}

	interface SuspendableMultimapCursor {
		AddedChangedRemoved<? extends Comparable<String>, JsonObject> cursor();

		@Suspendable
		List<JsonObject> initial();
	}
}
