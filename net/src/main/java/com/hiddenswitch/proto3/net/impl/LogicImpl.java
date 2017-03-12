package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Decks;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.Service;
import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckCatalogue;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 1/30/17.
 */
public class LogicImpl extends Service<LogicImpl> implements Logic {
	private ServiceProxy<Inventory> inventory;
	private ServiceProxy<Decks> decks;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		Broker.of(this, Logic.class, vertx.eventBus());
		inventory = Broker.proxy(Inventory.class, vertx.eventBus());
		decks = Broker.proxy(Decks.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException {
		final InitializeUserResponse response = new InitializeUserResponse();
		response.setDeckCreateResponses(new ArrayList<>());
		final String userId = request.getUserId();
		final CreateCollectionRequest startingCollection = CreateCollectionRequest.startingCollection(userId);
		response.setCreateCollectionResponse(inventory.sync()
				.createCollection(startingCollection));

		// Create the starting decks
		try {
			DeckCatalogue.loadDecksFromPackage();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException();
		}

		if (DeckCatalogue.getDecks().size() > 0) {
			for (String deckName : new String[]{"Basic Resurrector", "Basic Chef"}) {
				Deck deck = DeckCatalogue.getDeckByName(deckName);
				if (deck == null) {
					continue;
				}
				// Give the player the cards they need for their starting decks
				final AddToCollectionResponse addToCollectionResponse = inventory.sync().addToCollection(new AddToCollectionRequest()
						.withUserId(userId)
						.withCardIds(deck.getCards().toList().stream().map(Card::getCardId).collect(Collectors.toList())));
				final DeckCreateResponse deckCreate = decks.sync().createDeck(new DeckCreateRequest()
						.withUserId(userId)
						.withHeroClass(deck.getHeroClass())
						.withName(deck.getName())
						.withInventoryIds(addToCollectionResponse.getInventoryIds()));
				response.getDeckCreateResponses().add(deckCreate);
				response.getCreateCollectionResponse().getCreatedInventoryIds().addAll(addToCollectionResponse.getInventoryIds());
			}
		}


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
			deckCollection.getInventoryRecords().stream()
					.map(cardRecord -> getDescriptionFromRecord(cardRecord, player.getUserId(), player.getDeckId()))
					.map(CardDesc::createInstance)
					.forEach(deck.getCards()::add);

			// TODO: Add player information as attached to the hero card
			response.getPlayers().set(player.getId(), new StartGameResponse.Player()
					.withDeck(deck));

		}

		// Create a trigger that handles the appropriate game events
		final TriggerDesc statisticsTriggers = new TriggerDesc();
		final Map<EventTriggerArg, Object> args = new HashMap<>();
		args.put(EventTriggerArg.CLASS, "GameStateChangedTrigger");
		final EventTriggerDesc eventTriggerDesc = new EventTriggerDesc(args);

		// Borrow the decks
		final List<String> deckIds = request.getPlayers().stream().map(StartGameRequest.Player::getDeckId).collect(Collectors.toList());
		inventory.sync().borrowFromCollection(new BorrowFromCollectionRequest().withCollectionIds(deckIds));

		return response;
	}

	@Suspendable
	@Override
	public LogicResponse beforeSummon(EventLogicRequest<BeforeSummonEvent> request) {
		LogicResponse response = new LogicResponse();
		final String userId = request.getUserId();
		final String gameId = request.getGameId();
		final String id = request.getCardInventoryId();
		final int entityId = request.getEntityId();
		final BeforeSummonEvent beforeSummonEvent = request.getEvent();

		if (beforeSummonEvent == null
				|| beforeSummonEvent.getEventType() != GameEventType.BEFORE_SUMMON) {
			throw new RuntimeException();
		}

		// Add a unique champion ID only if this is the first time this champion has been added
		MongoClientUpdateResult update1 = awaitResult(h -> getMongo()
				.updateCollection(Inventory.INVENTORY,
						json("_id", id, "facts.uniqueChampionIds", json("$ne", userId)),
						json("$addToSet", json("facts.uniqueChampionIds", userId), "$inc", json("facts.uniqueChampionIdsSize", 1)),
						h));

		// Notify that a fact has changed
		final boolean updated = update1.getDocModified() > 0L;

		if (updated) {
			response.withGameIdsAffected(Collections.singletonList(gameId))
					.withEntityIdsAffected(Collections.singletonList(entityId));
			AttributeMap map = new AttributeMap();
			map.put(Attribute.FIRST_TIME_PLAYS, beforeSummonEvent.getMinion().getAttributeValue(Attribute.FIRST_TIME_PLAYS) + 1);
			response.getModifiedAttributes().put(new EntityReference(entityId), map);
		} else {
			response.withGameIdsAffected(Collections.emptyList());
			response.withEntityIdsAffected(Collections.emptyList());
			response.withModifiedAttributes(Collections.emptyMap());
		}

		return response;
	}

	public static CardDesc getDescriptionFromRecord(InventoryRecord cardRecord, String userId, String deckId) {
		// Set up the attributes
		CardDesc desc = cardRecord.getCardDesc();
		if (desc.attributes == null) {
			desc.attributes = new AttributeMap();
		}
		desc.attributes.put(Attribute.USER_ID, userId);
		desc.attributes.put(Attribute.CARD_INVENTORY_ID, cardRecord.getId());
		desc.attributes.put(Attribute.DECK_ID, deckId);
		desc.attributes.put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
		desc.attributes.put(Attribute.CHAMPION_ID, userId);
		desc.attributes.put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
		desc.attributes.put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
		// Collect the facts
		desc.attributes.put(Attribute.FIRST_TIME_PLAYS, cardRecord.getFirstTimePlays());
		desc.attributes.put(Attribute.ENTITY_INSTANCE_ID, RandomStringUtils.randomAlphanumeric(20).toLowerCase());
		return desc;
	}


}
