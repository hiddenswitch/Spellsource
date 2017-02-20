package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bberman on 1/30/17.
 */
public class LogicImpl extends Service<LogicImpl> implements Logic {
	private ServiceProxy<Inventory> inventory;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		Broker.of(this, Logic.class, vertx.eventBus());
		inventory = Broker.proxy(Inventory.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException {
		final InitializeUserResponse response = new InitializeUserResponse();
		final String userId = request.getUserId();

		response.createCollectionResponse = inventory.sync()
				.createCollection(CreateCollectionRequest.startingCollection(userId));

		return response;
	}

	@Override
	public EndGameResponse endGame(EndGameRequest request) throws SuspendExecution, InterruptedException {
		// Return all currently borrowed decks
		final List<String> deckIds = request.getPlayers().stream().map(EndGameRequest.Player::getDeckId).collect(Collectors.toList());
		inventory.sync().returnToCollection(new ReturnToCollectionRequest().withDeckIds(deckIds));

		return new EndGameResponse();
	}

	@Override
	@Suspendable
	public StartGameResponse startGame(StartGameRequest request) throws SuspendExecution, InterruptedException {
		StartGameResponse response = new StartGameResponse();

		// Create the decks
		for (StartGameRequest.Player player : request.getPlayers()) {
			GetCollectionResponse deckCollection = inventory.sync().getCollection(new GetCollectionRequest()
					.withUserId(player.getUserId())
					.withDeckId(player.getDeckId()));

			// TODO: Check that we're not using an already borrowed deck!

			// Create the deck and assign all the appropriate IDs to the cards
			Deck deck = new Deck(deckCollection.getHeroClass());
			deck.setName(deckCollection.getName());
			deckCollection.getCardRecords().stream()
					.map(cardRecord -> {
						// Set up the attributes
						CardDesc desc = cardRecord.getCardDesc();
						if (desc.attributes == null) {
							desc.attributes = new AttributeMap();
						}
						desc.attributes.put(Attribute.CARD_INSTANCE_ID, cardRecord.getId());
						desc.attributes.put(Attribute.DECK_ID, player.getDeckId());
						desc.attributes.put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
						desc.attributes.put(Attribute.CHAMPION_ID, player.getUserId());
						desc.attributes.put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
						desc.attributes.put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
						return desc;
					})
					.map(CardDesc::createInstance)
					.map(instance -> {
						instance.getAttributes().put(Attribute.ENTITY_INSTANCE_ID, RandomStringUtils.randomAlphanumeric(20).toLowerCase());
						return instance;
					})
					.forEach(deck.getCards()::add);

			// TODO: Add player information as attached to the hero card
			response.getPlayers().set(player.getId(), new StartGameResponse.Player()
					.withDeck(deck));

		}

		// Borrow the decks
		final List<String> deckIds = request.getPlayers().stream().map(StartGameRequest.Player::getDeckId).collect(Collectors.toList());
		inventory.sync().borrowFromCollection(new BorrowFromCollectionRequest().withCollectionIds(deckIds));

		return response;
	}

}
