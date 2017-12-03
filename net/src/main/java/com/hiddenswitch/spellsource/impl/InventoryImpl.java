package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Cards;
import com.hiddenswitch.spellsource.Inventory;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkWriteOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.mongo.WriteOption;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.function.Function;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.*;

/**
 * Created by bberman on 1/19/17.
 */
public class InventoryImpl extends AbstractService<InventoryImpl> implements Inventory {
	private RpcClient<Cards> cards;
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		cards = Rpc.connect(Cards.class, vertx.eventBus());
		List<String> collections = mongo().getCollections();

		if (!collections.contains(INVENTORY)) {
			mongo().createCollection(INVENTORY);
		}

		if (!collections.contains(COLLECTIONS)) {
			mongo().createCollection(COLLECTIONS);
		}

		mongo().createIndex(INVENTORY, json("userId", 1));
		mongo().createIndex(INVENTORY, json("collectionIds", 1));

		registration = Rpc.register(this, Inventory.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public OpenCardPackResponse openCardPack(OpenCardPackRequest request) throws SuspendExecution, InterruptedException {
		QueryCardsRequest commons = new QueryCardsRequest()
				.withFields(CardFields.ALL)
				.withSets(CardSet.SPELLSOURCE)
				.withRarity(Rarity.COMMON)
				.withRandomCount((request.getCardsPerPack() - 1) * request.getNumberOfPacks());

		QueryCardsRequest allianceRares = new QueryCardsRequest()
				.withFields(CardFields.ALL)
				.withSets(CardSet.SPELLSOURCE)
				.withRarity(Rarity.ALLIANCE)
				.withRandomCount(request.getNumberOfPacks());

		QueryCardsResponse response = cards.sync()
				.queryCards(new QueryCardsRequest()
						.withRequests(commons, allianceRares));

		List<String> ids = createCardsForUser(request.getUserId(), response.getRecords().stream().map(CardCatalogueRecord::getJson).collect(toList()));
		return new OpenCardPackResponse(ids);
	}

	@Override
	@Suspendable
	public CreateCollectionResponse createCollection(final CreateCollectionRequest request) throws SuspendExecution, InterruptedException {
		final String userId = request.getUserId();
		switch (request.getType()) {
			case USER:
				if (userId == null) {
					throw new RuntimeException();
				}
				Long count = mongo().count(COLLECTIONS, json("_id", userId));

				if (count.equals(0L)) {
					String ignore = mongo().insert(COLLECTIONS, QuickJson.toJson(CollectionRecord.user(userId)));
				}
				List<String> newInventoryIds = new ArrayList<>();
				if (request.getQueryCardsRequest() != null) {
					int copies = request.getCopies();
					final QueryCardsResponse cardsResponse = cards.sync().queryCards(request.getQueryCardsRequest());
					List<JsonObject> cardsToAdd = cardsResponse.getRecords().stream().map(CardCatalogueRecord::getJson).collect(toList());
					newInventoryIds.addAll(createCardsForUser(userId, cardsToAdd, copies));
				}
				if (request.getCardIds() != null
						&& request.getCardIds().size() != 0) {
					newInventoryIds.addAll(createCardsForUser(request.getCardIds(), userId));
				}

				if (request.getOpenCardPackRequest() != null) {
					newInventoryIds.addAll(openCardPack(request.getOpenCardPackRequest()).getCreatedInventoryIds());
				}

				return CreateCollectionResponse.user(userId, newInventoryIds);
			case DECK:
				CollectionRecord record1 = CollectionRecord.deck(userId, request.getName(), request.getHeroClass(), request.isDraft());
				record1.setHeroCardId(request.getHeroCardId());
				final String deckId = mongo().insert(COLLECTIONS, QuickJson.toJson(record1));

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
				final String allianceId = awaitResult(h -> getMongo().insert(COLLECTIONS, QuickJson.toJson(record2), h));

				return CreateCollectionResponse.alliance(allianceId);
			default:
				throw new RuntimeException();
		}
	}

	protected List<String> createCardsForUser(final String userId, final List<JsonObject> cardsToAdd, int copies) throws InterruptedException, SuspendExecution {
		if (userId == null) {
			throw new NullPointerException();
		}

		List<String> userIdCollection = Collections.singletonList(userId);
		List<JsonObject> documents = Collections.nCopies(copies, cardsToAdd)
				.stream()
				.flatMap(Collection::stream)
				.map(card -> new InventoryRecord(RandomStringUtils.randomAlphanumeric(36), card)
						.withUserId(userId)
						.withCollectionIds(userIdCollection))
				.map(QuickJson::toJson)
				.collect(toList());

		mongo().insertManyWithOptions(INVENTORY, documents, new BulkWriteOptions().setOrdered(false).setWriteOption(WriteOption.ACKNOWLEDGED));

		return documents.stream().map(o -> o.getString("_id")).collect(toList());
	}

	protected List<String> createCardsForUser(final String userId, final List<JsonObject> cardsToAdd) throws InterruptedException, SuspendExecution {
		return createCardsForUser(userId, cardsToAdd, 1);
	}

	protected List<String> createCardsForUser(final List<String> cardIds, final String userId, int copies) throws InterruptedException, SuspendExecution {
		return createCardsForUser(userId,
				cards.sync().queryCards(new QueryCardsRequest().withCardIds(cardIds)).getRecords().stream().map(CardCatalogueRecord::getJson).collect(toList()), copies);
	}

	protected List<String> createCardsForUser(final List<String> cardIds, final String userId) throws InterruptedException, SuspendExecution {
		return createCardsForUser(cardIds, userId, 1);
	}

	@Override
	public AddToCollectionResponse addToCollection(AddToCollectionRequest request) throws SuspendExecution, InterruptedException {
		List<String> inventoryIds;
		String collectionId;
		if (request.getInventoryIds() != null) {
			inventoryIds = request.getInventoryIds();
			collectionId = request.getCollectionId();
		} else if (request.getCardIds() != null
				&& request.getUserId() != null) {
			inventoryIds = createCardsForUser(request.getCardIds(), request.getUserId(), request.getCopies());
			collectionId = request.getUserId();
		} else {
			throw new RuntimeException();
		}

		MongoClientUpdateResult r = awaitResult(h -> getMongo().updateCollectionWithOptions(Inventory.INVENTORY,
				json("_id", json("$in", inventoryIds)),
				json("$addToSet", json("collectionIds", collectionId)),
				new UpdateOptions().setMulti(true),
				h));

		return new AddToCollectionResponse(r, inventoryIds);
	}

	@Override
	public RemoveFromCollectionResponse removeFromCollection(RemoveFromCollectionRequest request) throws SuspendExecution, InterruptedException {
		MongoClientUpdateResult r = awaitResult(h -> getMongo().updateCollectionWithOptions(Inventory.INVENTORY,
				json("_id", json("$in", request.getInventoryIds())),
				json("$pull", json("collectionIds", request.getCollectionId())),
				new UpdateOptions().setMulti(true),
				h));

		return new RemoveFromCollectionResponse(r);
	}

	@Override
	public DonateToCollectionResponse donateToCollection(DonateToCollectionRequest request) throws SuspendExecution, InterruptedException {
		MongoClientUpdateResult r = awaitResult(h -> getMongo().updateCollection(
				Inventory.INVENTORY,
				json("_id", json("$in", request.getInventoryIds())),
				json("$set", json("allianceId", request.getAllianceId()),
						"$addToSet", json("collectionIds", request.getAllianceId())), h));

		return new DonateToCollectionResponse();
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
				json("$set", json("borrowed", true, "borrowedByUserId", request.getUserId())),
				new UpdateOptions().setMulti(true),
				h));

		return new BorrowFromCollectionResponse(update.getDocModified());
	}

	@Override
	@Suspendable
	public ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request) {
		MongoClientUpdateResult update = awaitResult(h -> getMongo().updateCollectionWithOptions(INVENTORY,
				json("collectionIds", json("$in", request.getDeckIds())),
				json("$set", json("borrowed", false, "borrowedByUserId", null)),
				new UpdateOptions().setMulti(true),
				h));

		return new ReturnToCollectionResponse();
	}

	@Override
	@Suspendable
	public GetCollectionResponse getCollection(GetCollectionRequest request) throws SuspendExecution, InterruptedException {
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
				responses.add(GetCollectionResponse.deck(record.getUserId(), deckId, record.getName(), record.getHeroClass(), deckInventories.get(deckId), record.isTrashed(), record.getDeckType(), record.getHeroCardId()));
			});

			return GetCollectionResponse.batch(responses);
		}

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
		final List<InventoryRecord> inventoryRecords = results.stream().map(r -> QuickJson.fromJson(r, InventoryRecord.class)).collect(toList());

		if (type == CollectionTypes.DECK) {
			CollectionRecord deck = mongo().findOne(COLLECTIONS, json("_id", collectionId), CollectionRecord.class);
			return GetCollectionResponse.deck(deck.getUserId(), request.getDeckId(), deck.getName(), deck.getHeroClass(), inventoryRecords, deck.isTrashed(), deck.getDeckType(), deck.getHeroCardId());
		} else /* if (type == CollectionTypes.USER) */ {
			return GetCollectionResponse.user(request.getUserId(), inventoryRecords);
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
				.updateCollectionWithOptions(Inventory.INVENTORY,
						json("collectionId", collectionId, "_id", json("$nin", setCollectionRequest.getInventoryIds())),
						json("$pull", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true), h));

		MongoClientUpdateResult r2 = awaitResult(h -> getMongo()
				.updateCollectionWithOptions(Inventory.INVENTORY,
						json("_id", json("$in", setCollectionRequest.getInventoryIds())),
						json("$addToSet", json("collectionIds", collectionId)),
						new UpdateOptions().setMulti(true), h));

		return new SetCollectionResponse(r2, r);
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
