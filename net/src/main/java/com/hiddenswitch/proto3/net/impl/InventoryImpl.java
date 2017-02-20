package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Cards;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.SetCollectionResponse;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.impl.util.CollectionRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.QuickJson.*;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 1/19/17.
 */
public class InventoryImpl extends Service<InventoryImpl> implements Inventory {
	public static final String INVENTORY = "inventory.cards";
	public static final String COLLECTIONS = "inventory.collections";
	private ServiceProxy<Cards> cards;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		Broker.of(this, Inventory.class, vertx.eventBus());
		cards = Broker.proxy(Cards.class, vertx.eventBus());
		List<String> collections = awaitResult(h -> getMongo().getCollections(h));
		if (!collections.contains(INVENTORY) || !collections.contains(COLLECTIONS)) {
			Void ignore = awaitResult(h -> getMongo().createCollection(INVENTORY, h));
			ignore = awaitResult(h -> getMongo().createCollection(COLLECTIONS, h));
			ignore = awaitResult(h -> getMongo().createIndex(INVENTORY, json("userId", 1), h));
			ignore = awaitResult(h -> getMongo().createIndex(INVENTORY, json("collectionIds", 1), h));
		}
	}

	@Override
	@Suspendable
	public OpenCardPackResponse openCardPack(OpenCardPackRequest request) throws SuspendExecution, InterruptedException {
		QueryCardsRequest commons = new QueryCardsRequest()
				.withFields(CardFields.ALL)
				.withSets(CardSet.MINIONATE)
				.withRarity(Rarity.COMMON)
				.withRandomCount((request.getCardsPerPack() - 1) * request.getNumberOfPacks());

		QueryCardsRequest allianceRares = new QueryCardsRequest()
				.withFields(CardFields.ALL)
				.withSets(CardSet.MINIONATE)
				.withRarity(Rarity.ALLIANCE)
				.withRandomCount(request.getNumberOfPacks());

		QueryCardsResponse response = cards.sync()
				.queryCards(new QueryCardsRequest()
						.withRequests(commons, allianceRares));

		createCardsForUser(request.getUserId(), response.getRecords().stream().map(CardCatalogueRecord::getJson).collect(Collectors.toList()));
		return new OpenCardPackResponse();
	}

	@Override
	@Suspendable
	public CreateCollectionResponse createCollection(CreateCollectionRequest request) throws SuspendExecution, InterruptedException {
		switch (request.getType()) {
			case USER:
				Long count = awaitResult(h -> getMongo().count(COLLECTIONS, json("_id", request.getUserId()), h));

				if (count.equals(0L)) {
					String ignore = awaitResult(h -> getMongo().insert(COLLECTIONS, toJson(CollectionRecord.user(request.getUserId())), h));
				}

				if (request.getQueryCardsRequest() != null) {
					final QueryCardsResponse cardsResponse = cards.sync().queryCards(request.getQueryCardsRequest());
					List<JsonObject> cardsToAdd = cardsResponse.getRecords().stream().map(CardCatalogueRecord::getJson).collect(Collectors.toList());
					createCardsForUser(request.getUserId(), cardsToAdd);
				}

				if (request.getOpenCardPackRequest() != null) {
					openCardPack(request.getOpenCardPackRequest());
				}

				return CreateCollectionResponse.user(request.getUserId());
			case DECK:
				CollectionRecord record = CollectionRecord.deck(request.getUserId(), request.getName(), request.getHeroClass());
				final String deckId = awaitResult(h -> getMongo().insert(COLLECTIONS, toJson(record), h));

				if (request.getInventoryIds() != null
						&& request.getInventoryIds().size() > 0) {
					final MongoClientUpdateResult update = awaitResult(h -> getMongo().updateCollectionWithOptions(INVENTORY,
							json("_id", json("$in", request.getInventoryIds())),
							json("$addToSet", json("collectionIds", deckId)),
							new UpdateOptions().setMulti(true),
							h));
				}

				return CreateCollectionResponse.deck(deckId);
			default:
				throw new RuntimeException();
		}
	}

	@Suspendable
	protected void createCardsForUser(final String userId, final List<JsonObject> cardsToAdd) {
		if (userId == null) {
			throw new NullPointerException();
		}

		for (JsonObject card : cardsToAdd) {
			CardRecord cardRecord = new CardRecord(card)
					.withUserId(userId)
					.withCollectionIds(Collections.singletonList(userId));

			String id = awaitResult(h -> getMongo().insert(INVENTORY, toJson(cardRecord), h));
		}
	}

	@Override
	public AddToCollectionResponse addToCollection(AddToCollectionRequest request) throws SuspendExecution, InterruptedException {
		MongoClientUpdateResult r = awaitResult(h -> getMongo().updateCollectionWithOptions(InventoryImpl.INVENTORY,
				json("_id", json("$in", request.getInventoryIds())),
				json("$addToSet", json("collectionIds", request.getCollectionId())),
				new UpdateOptions().setMulti(true),
				h));

		return new AddToCollectionResponse(r);
	}

	@Override
	public RemoveFromCollectionResponse removeFromCollection(RemoveFromCollectionRequest request) throws SuspendExecution, InterruptedException {
		MongoClientUpdateResult r = awaitResult(h -> getMongo().updateCollectionWithOptions(InventoryImpl.INVENTORY,
				json("_id", json("$in", request.getInventoryIds())),
				json("$pull", json("collectionIds", request.getCollectionId())),
				new UpdateOptions().setMulti(true),
				h));

		return new RemoveFromCollectionResponse(r);
	}

	@Override
	@Suspendable
	public BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) throws SuspendExecution, InterruptedException {
		List<String> collectionIds;
		if (request.getCollectionId() != null) {
			collectionIds = Collections.singletonList(request.getCollectionId());
		} else if (request.getCollectionIds() != null) {
			collectionIds = request.getCollectionIds();
		} else {
			throw new RuntimeException();
		}

		MongoClientUpdateResult update = awaitResult(h -> getMongo().updateCollectionWithOptions(INVENTORY,
				json("collectionIds", json("$in", collectionIds)),
				json("$set", json("borrowed", true)),
				new UpdateOptions().setMulti(true),
				h));

		return new BorrowFromCollectionResponse(update.getDocModified());
	}

	@Override
	@Suspendable
	public ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public GetCollectionResponse getCollection(GetCollectionRequest request) throws SuspendExecution, InterruptedException {
		final String collectionId;
		final CollectionTypes type;
		if (request.getUserId() != null
				&& request.getDeckId() == null) {
			collectionId = request.getUserId();
			type = CollectionTypes.USER;
		} else if (request.getDeckId() != null) {
			collectionId = request.getDeckId();
			type = CollectionTypes.DECK;
		} else {
			collectionId = null;
			type = null;
		}

		if (collectionId == null) {
			throw new RuntimeException();
		}

		List<JsonObject> results = awaitResult(h -> getMongo().find(INVENTORY, json("collectionIds", collectionId), h));
		final List<CardRecord> cardRecords = results.stream().map(r -> fromJson(r, CardRecord.class)).collect(Collectors.toList());

		if (type == CollectionTypes.DECK) {
			List<JsonObject> deckCollection = awaitResult(h -> getMongo().find(COLLECTIONS, json("_id", collectionId), h));
			if (deckCollection.size() == 0) {
				throw new RuntimeException();
			}

			CollectionRecord deck = fromJson(deckCollection.get(0), CollectionRecord.class);
			return GetCollectionResponse.deck(deck.getUserId(), request.getDeckId(), deck.getName(), deck.getHeroClass(), cardRecords);
		} else /* if (type == CollectionTypes.USER) */ {
			return GetCollectionResponse.user(request.getUserId(), cardRecords);
		} /*  else {
			return new GetCollectionResponse()
					.withCardRecords(cardRecords);
		} */
	}

	@Override
	public TrashCollectionResponse trashCollection(TrashCollectionRequest request) throws SuspendExecution, InterruptedException {
		final String collectionId = request.getCollectionId();

		MongoClientUpdateResult result1 = awaitResult(h -> getMongo()
				.updateCollection(COLLECTIONS,
						json("_id", collectionId, "trashed", false),
						json("$set", json("trashed", true)), h));

		MongoClientUpdateResult result2 = awaitResult(h -> getMongo()
				.updateCollectionWithOptions(INVENTORY,
						json("collectionIds", collectionId),
						json("$pull", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true), h));

		return new TrashCollectionResponse(result1.getDocModified() == 1, result2.getDocModified());
	}

	@Override
	public SetCollectionResponse setCollection(SetCollectionRequest setCollectionRequest) throws SuspendExecution, InterruptedException {
		String collectionId = setCollectionRequest.getCollectionId();
		MongoClientUpdateResult r = awaitResult(h -> getMongo()
				.updateCollectionWithOptions(InventoryImpl.INVENTORY,
						json("collectionId", collectionId, "_id", json("$nin", setCollectionRequest.getInventoryIds())),
						json("$pull", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true), h));

		MongoClientUpdateResult r2 = awaitResult(h -> getMongo()
				.updateCollectionWithOptions(InventoryImpl.INVENTORY,
						json("_id", json("$in", setCollectionRequest.getInventoryIds())),
						json("$addToSet", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true), h));

		return new SetCollectionResponse(r2, r);
	}
}
