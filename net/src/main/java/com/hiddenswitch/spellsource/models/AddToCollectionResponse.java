package com.hiddenswitch.spellsource.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by bberman on 1/19/17.
 */
public class AddToCollectionResponse implements Serializable {
	private final Map<String, Object> updateResult;
	private final List<String> inventoryIds;

	@JsonIgnore
	public MongoClientUpdateResult getUpdateResult() {
		return new MongoClientUpdateResult(new JsonObject(updateResult));
	}

	public AddToCollectionResponse(MongoClientUpdateResult updateResult, List<String> inventoryIds) {
		this.updateResult = updateResult.toJson().getMap();
		this.inventoryIds = inventoryIds;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}
}
