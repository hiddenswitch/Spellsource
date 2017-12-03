package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.impl.util.DeckType;
import com.hiddenswitch.spellsource.Decks;
import com.hiddenswitch.spellsource.Inventory;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommand;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.QuickJson.jsonPut;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.*;

/**
 * Created by bberman on 2/4/17.
 */
public class DecksImpl extends AbstractService<DecksImpl> implements Decks {
	private RpcClient<Inventory> inventory;
	private Registration registration;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		inventory = Rpc.connect(Inventory.class, vertx.eventBus());
		// Create the starting decks
		try {
			CardCatalogue.loadCardsFromPackage();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException();
		} catch (CardParseException e) {
			e.printStackTrace();
		}

		registration = Rpc.register(this, Decks.class, vertx.eventBus());
	}

	@Override
	public DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException {
		if (request.getUserId() == null) {
			throw new SecurityException();
		}

		if (!request.isValid()) {
			throw new IllegalArgumentException("Invalid deck creation request.");
		}

		List<String> inventoryIds = new ArrayList<>();
		if (request.getInventoryIds() != null) {
			inventoryIds.addAll(request.getInventoryIds());
		}

		if (request.getCardIds() != null
				&& request.getCardIds().size() > 0) {
			// Find the card IDs in the user's collection, using copies wherever available, to put into the deck
			GetCollectionResponse userCollection = inventory.sync().getCollection(GetCollectionRequest.user(request.getUserId()));
			Map<String, List<String>> cards = userCollection.getInventoryRecords().stream().collect(groupingBy(InventoryRecord::getCardId, mapping(InventoryRecord::getId, toList())));

			for (String cardId : request.getCardIds()) {
				List<String> entry = cards.getOrDefault(cardId, Collections.emptyList());
				if (entry.size() == 0) {
					// TODO: Create a copy of the card on the fly for now.
					cards.put(cardId, inventory.sync().addToCollection(new AddToCollectionRequest()
							.withCardIds(Collections.singletonList(cardId))
							.withUserId(request.getUserId())
							// Add just one copy for now (add cards on demand)
							.withCopies(1)).getInventoryIds());
					entry = cards.get(cardId);
				}
				String record = entry.remove(0);
				inventoryIds.add(record);
			}
		}

		if (inventoryIds.size() > getMaxDeckSize()) {
			throw new RuntimeException();
		} else {
			final int size = request.getInventoryIds() != null ? request.getInventoryIds().size() : 0;
			final int cardCount = request.getCardIds() != null ? request.getCardIds().size() : 0;
			if (inventoryIds.size() != (size + cardCount)) {
				throw new RuntimeException();
			}
		}

		// Creates a new collection representing this deck
		final String userId = request.getUserId();
		CreateCollectionResponse createCollectionResponse = inventory.sync()
				.createCollection(CreateCollectionRequest.deck(userId, request.getName(), request.getHeroClass(), inventoryIds, request.isDraft())
						.withHeroCardId(request.getHeroCardId()));

		// Update the user document with this deck ID
		final String deckId = createCollectionResponse.getCollectionId();
		Accounts.update(getMongo(), userId, json("$addToSet", json("decks", deckId)));

		// Get the collection
		GetCollectionResponse getCollectionResponse = inventory.sync()
				.getCollection(new GetCollectionRequest().withUserId(userId).withDeckId(createCollectionResponse.getCollectionId()));

		return new DeckCreateResponse(deckId, getCollectionResponse);
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
		final String deckId = request.getDeckId();

		List<JsonObject> decks = awaitResult(h -> getMongo().findWithOptions(Inventory.COLLECTIONS,
				json("_id", deckId),
				new FindOptions().setFields(json("userId", 1)),
				h));

		final String userId = decks.get(0).getString("userId");
		// Sets the deck to trashed
		TrashCollectionResponse response = inventory.sync().trashCollection(new TrashCollectionRequest(deckId));

		// Remove the deckId from the user's decks
		Accounts.update(getMongo(), userId, json("$pull", json("decks", deckId)));

		return new DeckDeleteResponse(response);
	}

	@Suspendable
	@Override
	public DeckListUpdateResponse updateAllDecks(DeckListUpdateRequest request) throws SuspendExecution, InterruptedException {
		// Get all the non-draft decks
		List<String> deckIds = mongo().findWithOptions(Inventory.COLLECTIONS, json("deckType", DeckType.CONSTRUCTED.toString()), new FindOptions().setFields(json("_id", true)))
				.stream().map(o -> o.getString("_id")).collect(toList());

		// Trash them all
		for (String deckId : deckIds) {
			deleteDeck(new DeckDeleteRequest(deckId));
		}

		// Get all the users
		List<String> userIds = mongo().findWithOptions(Accounts.USERS, json(), new FindOptions().setFields(json("_id", true))).stream().map(o -> o.getString("_id")).collect(toList());

		AtomicLong updated = new AtomicLong();

		// Add all the new decks
		// TODO: For now, with so few users, we're not going to overoptimize this. We'll just call the API methods.
		for (String userId : userIds) {
			for (DeckCreateRequest deckCreate : request.getDeckCreateRequests()) {
				createDeck(deckCreate.clone().withUserId(userId));
				updated.incrementAndGet();
			}
		}

		return new DeckListUpdateResponse(updated.get());
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}

}
