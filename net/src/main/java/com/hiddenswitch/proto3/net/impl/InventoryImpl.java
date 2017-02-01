package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Cards;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.Rarity;

import java.util.*;

/**
 * Created by bberman on 1/19/17.
 */
public class InventoryImpl extends Service<InventoryImpl> implements Inventory {
	// TODO: Use a proper database for this
	private Map<String, Set<InventoryRecord>> userInventory = new HashMap<>();
	private Map<String, InventoryRecord> inventory = new HashMap<>();
	private ServiceProxy<Cards> cards;

	@Override
	public void start() {
		Broker.of(this, Inventory.class, vertx.eventBus());
		cards = Broker.proxy(Cards.class, vertx.eventBus());
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

		giveCardsToUser(request.getUserId(), response.getCards());
		return new OpenCardPackResponse();
	}

	@Override
	@Suspendable
	public CreateCollectionResponse createCollection(CreateCollectionRequest request) throws InterruptedException, SuspendExecution {
		switch (request.getType()) {
			case USER:
				// The user can only have one collection
				final String userId = request.getUserId();
				if (!userInventory.containsKey(userId)) {
					userInventory.put(userId, new HashSet<>());
				}

				if (request.getQueryCardsRequest() != null) {
					final QueryCardsResponse cardsResponse = cards.sync().queryCards(request.getQueryCardsRequest());
					List<Card> cardsToAdd = cardsResponse.getCards();
					giveCardsToUser(userId, cardsToAdd);
				}

				if (request.getOpenCardPackRequest() != null) {
					openCardPack(request.getOpenCardPackRequest());
				}

				break;
		}

		return new CreateCollectionResponse();
	}

	@Suspendable
	protected void giveCardsToUser(String userId, List<Card> cardsToAdd) {
		for (Card card : cardsToAdd) {
			InventoryRecord cardRecord = new InventoryRecord(card);
			// Add the record to the user's inventory database and the all-game inventory
			inventory.put(cardRecord.instanceId, cardRecord);
			userInventory.get(userId).add(cardRecord);
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
	public GetCollectionResponse getCollection(GetCollectionRequest request) {
		if (request.getUserId() != null) {
			return new GetCollectionResponse()
					.withInventoryRecords(userInventory.get(request.getUserId()));
		}

		return new GetCollectionResponse();
	}
}
