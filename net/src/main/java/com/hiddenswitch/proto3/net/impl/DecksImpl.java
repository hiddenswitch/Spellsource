package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.client.models.DecksUpdateCommand;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static com.hiddenswitch.proto3.net.util.QuickJson.jsonPut;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 2/4/17.
 */
public class DecksImpl extends Service<DecksImpl> implements Decks {
	private ServiceProxy<Inventory> inventory;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		inventory = Broker.proxy(Inventory.class, vertx.eventBus());

		Broker.of(this, Decks.class, vertx.eventBus());
	}

	@Override
	public DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException {
		if (request.getInventoryIds() == null) {
			request.setInventoryIds(Collections.emptyList());
		}

		if (request.getInventoryIds().size() > getMaxDeckSize()) {
			throw new RuntimeException();
		}

		// Creates a new collection representing this deck
		CreateCollectionResponse createCollectionResponse = inventory.sync()
				.createCollection(CreateCollectionRequest.deck(request.getUserId(), request.getName(), request.getHeroClass(), request.getInventoryIds()));

		return new DeckCreateResponse(createCollectionResponse.getCollectionId());
	}

	private int getMaxDeckSize() {
		return 30;
	}

	@Override
	public DeckUpdateResponse updateDeck(DeckUpdateRequest request) throws SuspendExecution, InterruptedException {
		DecksUpdateCommand updateCommand = request.getUpdateCommand();
		String deckId = request.getDeckId();
		String userId = request.getUserId();
		MongoClient mongo = getMongo();
		JsonObject collectionUpdate = new JsonObject();

		if (updateCommand.getSetHeroClass() != null) {
			jsonPut(collectionUpdate, "$set", json("heroClass", updateCommand.getSetHeroClass()));
		}

		if (updateCommand.getSetName() != null) {
			jsonPut(collectionUpdate, "$set", json("name", updateCommand.getSetName()));
		}

		if (!collectionUpdate.isEmpty()) {
			MongoClientUpdateResult r = awaitResult(h -> mongo.updateCollection(Inventory.COLLECTIONS,
					json("_id", deckId, "userId", userId),
					collectionUpdate, h));
		}

		if (updateCommand.getPullAllInventoryIds() != null) {
			inventory.sync().removeFromCollection(new RemoveFromCollectionRequest(deckId, updateCommand.getPullAllInventoryIds()));
		}

		if (updateCommand.getPushInventoryIds() != null) {
			inventory.sync().addToCollection(new AddToCollectionRequest(deckId, updateCommand.getPushInventoryIds().getEach()));
		}

		if (updateCommand.getSetInventoryIds() != null) {
			// Remove cards, then add them back in
			List<String> inventoryIds = updateCommand.getSetInventoryIds();
			inventory.sync().setCollection(new SetCollectionRequest(deckId, inventoryIds));
		}
		return new DeckUpdateResponse();
	}

	@Override
	public DeckDeleteResponse deleteDeck(DeckDeleteRequest request) throws SuspendExecution, InterruptedException {
		TrashCollectionResponse response = inventory.sync().trashCollection(new TrashCollectionRequest(request.getDeckId()));
		return new DeckDeleteResponse(response);
	}

}
