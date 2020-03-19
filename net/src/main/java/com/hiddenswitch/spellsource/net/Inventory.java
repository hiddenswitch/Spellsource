package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.impl.QuickJson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.function.Function;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Provides methods to manage a player's persistent inventory.
 */
public interface Inventory {
	String INVENTORY = "inventory.cards";
	String COLLECTIONS = "inventory.collections";

	@Suspendable
	static CreateCollectionResponse createCollection(CreateCollectionRequest request) throws SuspendExecution, InterruptedException {
		final String userId = request.getUserId();
		switch (request.getType()) {
			case USER:
				if (userId == null) {
					throw new RuntimeException();
				}
				Long count = mongo().count(COLLECTIONS, json("_id", userId));

				if (count.equals(0L)) {
					String ignore = mongo().insert(COLLECTIONS, JsonObject.mapFrom(CollectionRecord.user(userId)));
				}
				List<String> newInventoryIds = new ArrayList<>();
				if (request.getQueryCardsRequest() != null) {
					int copies = request.getCopies();
					final QueryCardsResponse cardsResponse = Cards.query(request.getQueryCardsRequest());
					List<CardDesc> cardsToAdd = cardsResponse.getRecords().stream().map(CardCatalogueRecord::getDesc).collect(toList());
					newInventoryIds.addAll(createCardsForUser(userId, cardsToAdd, copies));
				}
				if (request.getCardIds() != null
						&& request.getCardIds().size() != 0) {
					newInventoryIds.addAll(createCardsForUser(request.getCardIds(), userId));
				}

				return CreateCollectionResponse.user(userId, newInventoryIds);
			case DECK:
				CollectionRecord record1 = CollectionRecord.deck(userId, request.getName(), request.getHeroClass(), request.isDraft());
				record1.setHeroCardId(request.getHeroCardId());
				record1.setFormat(request.getFormat());
				record1.setStandardDeck(request.isStandard());
				record1.setValidationReport(request.getValidationReport());
				final String deckId = mongo().insert(COLLECTIONS, JsonObject.mapFrom(record1));

				if (request.getInventoryIds() != null
						&& request.getInventoryIds().size() > 0) {
					MongoClientUpdateResult update = mongo()
							.updateCollectionWithOptions(INVENTORY,
									json("_id", json("$in", request.getInventoryIds())),
									json("$addToSet", json("collectionIds", deckId)),
									new UpdateOptions().setMulti(true));
				}

				return CreateCollectionResponse.deck(deckId);
			case ALLIANCE:
				CollectionRecord record2 = CollectionRecord.alliance(request.getAllianceId(), userId);
				String allianceId = mongo().insert(COLLECTIONS, JsonObject.mapFrom(record2));

				return CreateCollectionResponse.alliance(allianceId);
			default:
				throw new RuntimeException();
		}
	}

	static List<String> createCardsForUser(String userId, List<CardDesc> cardsToAdd, int copies) throws InterruptedException, SuspendExecution {
		if (userId == null) {
			throw new NullPointerException();
		}

		List<String> userIdCollection = Collections.singletonList(userId);
		List<JsonObject> documents = Collections.nCopies(copies, cardsToAdd)
				.stream()
				.flatMap(Collection::stream)
				.map(card -> new InventoryRecord(RandomStringUtils.randomAlphanumeric(36), new JsonObject().put("id", card.getId()))
						.withUserId(userId)
						.withCollectionIds(userIdCollection))
				.map(JsonObject::mapFrom)
				.collect(toList());

		mongo().insertManyWithOptions(INVENTORY, documents, new BulkWriteOptions().setOrdered(false).setWriteOption(WriteOption.ACKNOWLEDGED));

		return documents.stream().map(o -> o.getString("_id")).collect(toList());
	}

	static List<String> createCardsForUser(String userId, List<CardDesc> cardsToAdd) throws InterruptedException, SuspendExecution {
		return createCardsForUser(userId, cardsToAdd, 1);
	}

	static List<String> createCardsForUser(List<String> cardIds, String userId, int copies) throws InterruptedException, SuspendExecution {
		return createCardsForUser(userId,
				Cards.query(new QueryCardsRequest().withCardIds(cardIds))
						.getRecords().stream().map(CardCatalogueRecord::getDesc).collect(toList()), copies);
	}

	static List<String> createCardsForUser(List<String> cardIds, String userId) throws InterruptedException, SuspendExecution {
		return createCardsForUser(cardIds, userId, 1);
	}

	static AddToCollectionResponse addToCollection(AddToCollectionRequest request) throws SuspendExecution, InterruptedException {
		List<String> inventoryIds;
		String collectionId;
		if (request.getInventoryIds() != null) {
			// This is a request to add specific card IDs to the specific collection
			inventoryIds = request.getInventoryIds();
			collectionId = request.getCollectionId();
		} else if (request.getCardIds() != null
				&& request.getUserId() != null
				&& (Objects.equals(request.getCollectionId(), request.getUserId()) || request.getCollectionId() == null)) {
			// This is a request to create cards, because the cards are being added directly to the user's collection
			inventoryIds = createCardsForUser(request.getCardIds(), request.getUserId(), request.getCopies());
			collectionId = request.getUserId();
		} else if (request.getCardIds() != null
				&& request.getUserId() != null
				&& !Objects.equals(request.getCollectionId(), request.getUserId())) {
			// This is a request to add specific card IDs to the specified non-user collection. Only create cards if the
			// user doesn't already own sufficient copies.
			collectionId = request.getCollectionId();
			inventoryIds = new ArrayList<>();
			// Interpret duplicate card IDs as multiple copies
			Multiset<String> cardIds = HashMultiset.create(request.getCardIds());
			List<JsonObject> unusedInventories = mongo().findWithOptions(INVENTORY,
					json("userId", request.getUserId(),
							"collectionIds", json("$ne", request.getCollectionId()),
							"cardDesc.id", json("$in", request.getCardIds().stream().distinct().collect(toList()))),
					new FindOptions().setFields(json("_id", 1, "cardDesc.id", 1)));
			for (JsonObject unusedInventory : unusedInventories) {
				final String cardId = unusedInventory.getJsonObject("cardDesc").getString("id");
				if (cardIds.contains(cardId)) {
					inventoryIds.add(unusedInventory.getString("_id"));
					cardIds.remove(cardId);
				}
			}

			final ArrayList<String> cardIds1 = new ArrayList<>(cardIds);
			if (!cardIds1.isEmpty()) {
				inventoryIds.addAll(createCardsForUser(cardIds1, request.getUserId()));
			}
		} else {
			throw new RuntimeException();
		}

		MongoClientUpdateResult result = mongo().updateCollectionWithOptions(Inventory.INVENTORY,
				json("_id", json("$in", inventoryIds)),
				json("$addToSet", json("collectionIds", collectionId)),
				new UpdateOptions().setMulti(true));

		return AddToCollectionResponse.create(result, inventoryIds);
	}

	static RemoveFromCollectionResponse removeFromCollection(RemoveFromCollectionRequest request) throws SuspendExecution, InterruptedException {
		MongoClientUpdateResult result;

		if (request.getCollectionId() == null) {
			throw new IllegalArgumentException("No collection ID specified.");
		}

		if (request.getInventoryIds() == null && request.getCardIds() == null) {
			throw new IllegalArgumentException(String.format("No inventory or card IDs specified for removal request collectionId=%s", request.getCollectionId()));
		}

		List<String> inventoryIds = request.getInventoryIds() == null ? new ArrayList<>() : new ArrayList<>(request.getInventoryIds());
		if (request.getCardIds() != null) {
			// Find the corresponding inventory IDs in this collection to remove. The cards that get removed will be arbitrary. Interpret duplicates as the count
			List<JsonObject> existingInventoryIds = mongo().findWithOptions(INVENTORY,
					json("collectionIds", request.getCollectionId(),
							"cardDesc.id", json("$in", request.getCardIds())),
					new FindOptions().setFields(json("_id", 1, "cardDesc.id", 1)));

			Map<String, List<JsonObject>> cardsInCollection = existingInventoryIds.stream().collect(groupingBy(jo -> jo.getJsonObject("cardDesc").getString("id")));
			for (String cardId : request.getCardIds()) {
				final List<JsonObject> inventoryItemsForId = cardsInCollection.getOrDefault(cardId, Collections.emptyList());
				if (inventoryItemsForId.size() == 0) {
					throw new IllegalArgumentException(String.format("The collectionId=%s does not contain the cardId=%s", request.getCollectionId(), cardId));
				}

				inventoryIds.add(inventoryItemsForId.remove(0).getString("_id"));
			}
		}

		result = mongo().updateCollectionWithOptions(Inventory.INVENTORY,
				json("_id", json("$in", inventoryIds)),
				json("$pull", json("collectionIds", request.getCollectionId())),
				new UpdateOptions().setMulti(true));

		if (result.getDocMatched() != inventoryIds.size()) {
			throw new ArrayStoreException(String.format("Could not find the correct number of inventoryIds=%s to remove from collectionId=%s.", request.getInventoryIds().toString(), request.getCollectionId()));
		}

		return new RemoveFromCollectionResponse(result, inventoryIds);
	}


	static DonateToCollectionResponse donateToCollection(DonateToCollectionRequest request) throws SuspendExecution, InterruptedException {
		mongo().updateCollection(
				Inventory.INVENTORY,
				json("_id", json("$in", request.getInventoryIds())),
				json("$set", json("allianceId", request.getAllianceId()),
						"$addToSet", json("collectionIds", request.getAllianceId())));

		return new DonateToCollectionResponse();
	}

	@Suspendable
	static BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) throws SuspendExecution, InterruptedException {
		if (request.getUserId() == null) {
			throw new IllegalArgumentException("Must specify a userId");
		}

		List<String> collectionIds;
		if (request.getCollectionId() != null) {
			collectionIds = Collections.singletonList(request.getCollectionId());
		} else if (request.getCollectionIds() != null) {
			collectionIds = request.getCollectionIds();
		} else {
			throw new IllegalArgumentException("Must specify collectionIds");
		}

		MongoClientUpdateResult update = mongo().updateCollectionWithOptions(INVENTORY,
				json("collectionIds", json("$in", collectionIds)),
				json("$set", json("borrowed", true, "borrowedByUserId", request.getUserId())),
				new UpdateOptions().setMulti(true));

		return BorrowFromCollectionResponse.response(update.getDocModified());
	}

	/**
	 * Returns an exclusive right to use a card. The opposite of borrowing a card from a collection.
	 *
	 * @param request The cards to return.
	 * @return The result of returning the cards.
	 */
	@Suspendable
	static ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request) {
		mongo().updateCollectionWithOptions(INVENTORY,
				json("collectionIds", json("$in", request.getDeckIds())),
				json("$set", json("borrowed", false, "borrowedByUserId", null)),
				new UpdateOptions().setMulti(true));

		return new ReturnToCollectionResponse();
	}

	/**
	 * Gets the complete information about a user, alliance or deck collection.
	 *
	 * @param request Use the static methods in GetCollectionRequest for the right arguments of different collection
	 *                queries.
	 * @return The complete information about a collection (user, alliance or deck).
	 */
	@Suspendable
	static GetCollectionResponse getCollection(GetCollectionRequest request) {
		if (request.isBatchRequest()) {
			final List<GetCollectionResponse> responses = new ArrayList<>();
			final List<GetCollectionRequest> requests = request.getRequests();

			// Retrieve deck requests and process them separately
			final List<GetCollectionRequest> deckRequests = new ArrayList<>();
			Iterator<GetCollectionRequest> requestsIterator = requests.iterator();
			while (requestsIterator.hasNext()) {
				GetCollectionRequest subRequest = requestsIterator.next();
				if (subRequest.getDeckId() != null) {
					requestsIterator.remove();
					deckRequests.add(subRequest);
				} else {
					responses.add(getCollection(subRequest));
				}
			}

			// Bulk retrieve deck inventory records and collection information
			final List<String> deckIds = deckRequests.stream().map(GetCollectionRequest::getDeckId).collect(toList());
			final Map<String, CollectionRecord> deckRecords = mongo().find(COLLECTIONS, json("_id", json("$in", deckIds)), CollectionRecord.class)
					.stream().collect(toMap(CollectionRecord::getId, Function.identity()));

			final Map<String, List<InventoryRecord>> deckInventories = new HashMap<>();
			for (String deckId : deckIds) {
				deckInventories.put(deckId, new Vector<>());
			}

			mongo().find(INVENTORY, json("collectionIds", json("$in", deckIds)), InventoryRecord.class)
					.forEach(ir -> ir.getCollectionIds().forEach(cid -> {
						if (deckInventories.containsKey(cid)) {
							deckInventories.get(cid).add(ir);
						}
					}));

			deckIds.forEach(deckId -> {
				CollectionRecord record = deckRecords.get(deckId);
				responses.add(GetCollectionResponse.collection(record, deckInventories.get(deckId)));
			});

			return GetCollectionResponse.batch(responses);
		}

		String collectionId;
		CollectionTypes type;
		String userId = request.getUserId();
		if (userId != null
				&& request.getDeckId() == null) {
			collectionId = userId;
			type = CollectionTypes.USER;
		} else if (request.getDeckId() != null) {
			collectionId = request.getDeckId();
			type = CollectionTypes.DECK;
		} else {
			collectionId = null;
			type = null;
		}

		if (collectionId == null) {
			throw new NullPointerException("No collection was specified");
		}

		List<JsonObject> results = mongo().find(INVENTORY, json("collectionIds", collectionId));
		final List<InventoryRecord> inventoryRecords = results.stream().map(r -> QuickJson.fromJson(r, InventoryRecord.class)).collect(toList());

		CollectionRecord collection = mongo().findOne(COLLECTIONS, json("_id", collectionId), CollectionRecord.class);

		return GetCollectionResponse.collection(collection, inventoryRecords);
	}

	static TrashCollectionResponse trashCollection(TrashCollectionRequest request) throws SuspendExecution, InterruptedException {
		final String collectionId = request.getCollectionId();

		MongoClientUpdateResult result1 = mongo()
				.updateCollection(COLLECTIONS,
						json("_id", collectionId, "trashed", false),
						json("$set", json("trashed", true)));

		MongoClientUpdateResult result2 = mongo()
				.updateCollectionWithOptions(INVENTORY,
						json("collectionIds", collectionId),
						json("$pull", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true));

		return new TrashCollectionResponse(result1.getDocModified() == 1, result2.getDocModified());
	}

	static SetCollectionResponse setCollection(SetCollectionRequest setCollectionRequest) throws SuspendExecution, InterruptedException {
		String collectionId = setCollectionRequest.getCollectionId();
		MongoClientUpdateResult r = mongo()
				.updateCollectionWithOptions(Inventory.INVENTORY,
						json("collectionId", collectionId, "_id", json("$nin", setCollectionRequest.getInventoryIds())),
						json("$pull", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true));

		MongoClientUpdateResult r2 = mongo()
				.updateCollectionWithOptions(Inventory.INVENTORY,
						json("_id", json("$in", setCollectionRequest.getInventoryIds())),
						json("$addToSet", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true));

		return new SetCollectionResponse(r2, r);
	}

	@Suspendable
	static boolean isOwner(String collectionId, UserId userId) {
		return mongo().count(Inventory.COLLECTIONS, json("_id", collectionId, "userId", userId.toString())) != 0L;
	}
}
