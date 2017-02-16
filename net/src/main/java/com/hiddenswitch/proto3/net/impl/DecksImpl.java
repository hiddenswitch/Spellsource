package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.client.models.DecksUpdateCommand;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;

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
			MongoClientUpdateResult r = awaitResult(h -> mongo.updateCollection(InventoryImpl.COLLECTIONS,
					json("_id", deckId),
					collectionUpdate, h));
		}

		if (updateCommand.getPullAllInventoryIds() != null) {
			MongoClientUpdateResult r = awaitResult(h -> mongo.updateCollection(InventoryImpl.INVENTORY,
					json("_id", json("$in", updateCommand.getPullAllInventoryIds())),
					json("$pull", json("collectionIds", deckId)), h));

		} else if (updateCommand.getPushInventoryIds() != null) {
			MongoClientUpdateResult r = awaitResult(h -> mongo.updateCollection(InventoryImpl.INVENTORY,
					json("_id", json("$in", updateCommand.getPushInventoryIds().getEach())),
					json("$addToSet", json("collectionIds", deckId)), h));

		} else if (updateCommand.getSetInventoryIds() != null) {
			// Remove cards, then add them back in
			List<String> inventoryIds = updateCommand.getSetInventoryIds();

			MongoClientUpdateResult r = awaitResult(h -> mongo.updateCollection(InventoryImpl.INVENTORY,
					json("collectionId", deckId, "_id", json("$nin", inventoryIds)),
					json("$pull", json("collectionIds", deckId)), h));

			MongoClientUpdateResult r2 = awaitResult(h -> mongo.updateCollection(InventoryImpl.INVENTORY,
					json("_id", json("$in", inventoryIds)),
					json("$addToSet", json("collectionIds", deckId)), h));
		}
		return new DeckUpdateResponse();
	}

	@Override
	public DeckDeleteResponse deleteDeck(DeckDeleteRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public DeckUseResponse useDeck(DeckUseRequest request) throws SuspendExecution, InterruptedException {
//		GetCollectionResponse deckCollection = inventory.sync().getCollection(new GetCollectionRequest()
//				.withUserId(request.getUserId())
//				.withDeckId(request.getDeckId()));
//
//		// Create the deck and assign all the appropriate IDs to the cards
//		Deck deck = new Deck(deckCollection.getHeroClass());
//		deck.setDisplayName(deckCollection.getDisplayName());
//		deckCollection.getCardRecords().stream()
//				.map(cardRecord -> {
//					CardDesc desc = cardRecord.getCardDesc();
//					desc.attributes.put(Attribute.CARD_INSTANCE_ID, cardRecord.getId());
//					desc.attributes.put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
//					desc.attributes.put(Attribute.CHAMPION_ID, request.getUserId());
//					desc.attributes.put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
//					desc.attributes.put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
//					return desc;
//				})
//				.map(CardDesc::createInstance)
//				.forEach(deck.getCards()::add);

		return null;
	}

	@Override
	public DeckReturnResponse returnDeck(DeckReturnRequest request) {
		return null;
	}
}
