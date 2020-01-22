package com.hiddenswitch.spellsource.net.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by bberman on 2/16/17.
 */
public class RemoveFromCollectionResponse implements Serializable {
	private final Map<String, Object> updateResult;
	private final List<String> inventoryIds;

	@JsonIgnore
	public MongoClientUpdateResult getUpdateResult() {
		return new MongoClientUpdateResult(new JsonObject(updateResult));
	}

	public RemoveFromCollectionResponse(MongoClientUpdateResult updateResult, List<String> inventoryIds) {
		this.updateResult = updateResult.toJson().getMap();
		this.inventoryIds = inventoryIds;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}
}
