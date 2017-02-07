package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Cards;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.impl.util.CollectionRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;

import static io.vertx.ext.sync.Sync.*;
import static com.hiddenswitch.proto3.net.util.QuickJson.*;

import java.util.*;
import java.util.stream.Collectors;

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

		Void ignore = awaitResult(h -> getMongo().createCollection(INVENTORY, h));
		ignore = awaitResult(h -> getMongo().createCollection(COLLECTIONS, h));
		ignore = awaitResult(h -> getMongo().createIndex(INVENTORY, json("userId", 1), h));
		ignore = awaitResult(h -> getMongo().createIndex(INVENTORY, json("collectionIds", 1), h));
	}

	@Override
	@Suspendable
	public OpenCardPackResponse openCardPack(OpenCardPackRequest request) throws InterruptedException, SuspendExecution {
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

		createCardsForUser(request.getUserId(), response.getCards());
		return new OpenCardPackResponse();
	}

	@Override
	@Suspendable
	public CreateCollectionResponse createCollection(CreateCollectionRequest request) throws InterruptedException, SuspendExecution {
		switch (request.getType()) {
			case USER:
				Long count = awaitResult(h -> getMongo().count(COLLECTIONS, json("_id", request.getUserId()), h));

				if (count.equals(0L)) {
					String ignore = awaitResult(h -> getMongo().insert(COLLECTIONS, toJson(CollectionRecord.user(request.getUserId())), h));
				}

				if (request.getQueryCardsRequest() != null) {
					final QueryCardsResponse cardsResponse = cards.sync().queryCards(request.getQueryCardsRequest());
					List<Card> cardsToAdd = cardsResponse.getCards();
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
					final MongoClientUpdateResult update = awaitResult(h -> getMongo().updateCollection(INVENTORY,
							json("_id", json("$in", request.getInventoryIds())),
							json("$addToSet", json("collectionIds", deckId)), h));
				}

				return CreateCollectionResponse.deck(deckId);
			default:
				throw new RuntimeException();
		}
	}

	@Suspendable
	protected void createCardsForUser(final String userId, final List<Card> cardsToAdd) {
		for (Card card : cardsToAdd) {
			CardRecord cardRecord = new CardRecord(card)
					.withUserId(userId)
					.withCollectionIds(Collections.singletonList(userId));

			String id = awaitResult(h -> getMongo().insert(INVENTORY, toJson(cardRecord), h));
		}
	}

	@Override
	@Suspendable
	public AddToCollectionResponse addToCollection(AddToCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public GetCollectionResponse getCollection(GetCollectionRequest request) throws SuspendExecution {
		final String collection;
		final CollectionTypes type;
		if (request.getUserId() != null
				&& request.getDeckId() == null) {
			collection = request.getUserId();
			type = CollectionTypes.USER;
		} else if (request.getDeckId() != null) {
			collection = request.getDeckId();
			type = CollectionTypes.DECK;
		} else {
			collection = null;
			type = null;
		}

		if (collection == null) {
			return new GetCollectionResponse();
		}

		List<JsonObject> results = awaitResult(h -> getMongo().find(INVENTORY, json("collectionIds", collection), h));
		final List<CardRecord> cardRecords = results.stream().map(r -> fromJson(r, CardRecord.class)).collect(Collectors.toList());

		if (type == CollectionTypes.DECK) {
			List<JsonObject> deckCollection = awaitResult(h -> getMongo().find(COLLECTIONS, json("_id", collection), h));
			if (deckCollection.size() == 0) {
				throw new RuntimeException();
			}

			CollectionRecord deck = fromJson(deckCollection.get(0), CollectionRecord.class);
			return GetCollectionResponse.deck(deck.getName(), deck.getHeroClass(), cardRecords);
		} else /* if (type == CollectionTypes.USER) */ {
			return GetCollectionResponse.user(cardRecords);
		} /*  else {
			return new GetCollectionResponse()
					.withCardRecords(cardRecords);
		} */
	}
}
