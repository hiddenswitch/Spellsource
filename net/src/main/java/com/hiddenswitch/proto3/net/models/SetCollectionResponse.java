package com.hiddenswitch.proto3.net.models;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bberman on 2/17/17.
 */
public class SetCollectionResponse implements Serializable {
	private final Map<String, Object> added;
	private final Map<String, Object> removed;

	public MongoClientUpdateResult getAdded() {
		return new MongoClientUpdateResult(new JsonObject(added));
	}

	public MongoClientUpdateResult getRemoved() {
		return new MongoClientUpdateResult(new JsonObject(removed));
	}

	public SetCollectionResponse(MongoClientUpdateResult added, MongoClientUpdateResult removed) {
		this.added = added.toJson().getMap();
		this.removed = removed.toJson().getMap();
	}
}
