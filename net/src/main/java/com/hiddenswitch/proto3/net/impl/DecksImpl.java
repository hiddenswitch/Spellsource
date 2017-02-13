package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.impl.util.CardRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;

import java.util.Collections;

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
	public DeckUpdateResponse updateDeck(DeckUpdateRequest request) {
		return null;
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
//		deck.setName(deckCollection.getName());
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
