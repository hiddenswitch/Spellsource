package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.PutFactResponse;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bberman on 2/2/17.
 */
public interface Facts {
	/**
	 * Puts a fact into the facts database. Basically saves an event using the MongoDB update syntax.
	 * @param id The ID of the object being updated.
	 * @param update A MongoDB update command. This command is executed as an upsert.
	 * @return
	 */
	@Suspendable
	PutFactResponse putFact(String id, JsonObject update);
}
