package com.hiddenswitch.spellsource.net.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public final class AddToCollectionResponse implements Serializable {
	private Map<String, Object> updateResult;
	private List<String> inventoryIds;

	private AddToCollectionResponse() {
		updateResult = null;
		inventoryIds = null;
	}

	public static AddToCollectionResponse create(MongoClientUpdateResult updateResult, List<String> inventoryIds) {
		return new AddToCollectionResponse(updateResult, inventoryIds);
	}

	@JsonIgnore
	public MongoClientUpdateResult getUpdateResult() {
		return new MongoClientUpdateResult(new JsonObject(updateResult));
	}

	private AddToCollectionResponse(MongoClientUpdateResult updateResult, List<String> inventoryIds) {
		this.updateResult = updateResult.toJson().getMap();
		this.inventoryIds = inventoryIds;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}
}
