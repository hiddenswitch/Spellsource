package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.DecksUpdateCommand;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.util.DeckType;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.QuickJson.jsonPut;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * The deck management service.
 */
public interface Decks {
	Logger LOGGER = LoggerFactory.getLogger(Decks.class);

	static int getMaxDeckSize() {
		return 30;
	}

	/**
	 * Creates a deck with convenient arguments.
	 *
	 * @param request A request that specifies the contents of the deck either as card IDs or inventory IDs.
	 * @return The deck ID of the new deck. Conveniently matches a collection ID.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Decks/createDeck")
				.withTag("userId", request.getUserId())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			if (request.getUserId() == null) {
				throw new SecurityException("A valid userID is required.");
			}

			List<String> inventoryIds = new ArrayList<>();
			if (request.getInventoryIds() != null) {
				inventoryIds.addAll(request.getInventoryIds());
			}

			if (request.getCardIds() != null
					&& request.getCardIds().size() > 0) {
				// Find the card IDs in the user's collection, using copies wherever available, to put into the deck
				List<InventoryRecord> results = mongo().findWithOptions(Inventory.INVENTORY,
						json("collectionIds", request.getUserId()),
						new FindOptions().setFields(json(InventoryRecord.CARDDESC_ID, 1)),
						InventoryRecord.class);
				Map<String, List<String>> cards = results.stream().collect(groupingBy(InventoryRecord::getCardId, mapping(InventoryRecord::getId, toList())));

				for (String cardId : request.getCardIds()) {
					List<String> entry = cards.getOrDefault(cardId, Collections.emptyList());
					if (entry.size() == 0) {
						// TODO: Create a copy of the card on the fly for now.
						cards.put(cardId, Inventory.addToCollection(new AddToCollectionRequest()
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
				throw new RuntimeException(String.format("Cannot create a deck whose size %d exceeds %d", inventoryIds.size(), getMaxDeckSize()));
			} else {
				final int size = request.getInventoryIds() != null ? request.getInventoryIds().size() : 0;
				final int cardCount = request.getCardIds() != null ? request.getCardIds().size() : 0;
				if (inventoryIds.size() != (size + cardCount)) {
					throw new RuntimeException("Cannot create a deck that requested invalid card IDs");
				}
			}

			// Creates a new collection representing this deck
			final String userId = request.getUserId();
			CreateCollectionResponse createCollectionResponse = Inventory
					.createCollection(CreateCollectionRequest.deck(userId, request.getName(), request.getHeroClass(), inventoryIds, request.isDraft())
							.setStandard(request.isStandardDeck())
							.withHeroCardId(request.getHeroCardId())
							.withFormat(request.getFormat()));

			// Update the user document with this deck ID
			final String deckId = createCollectionResponse.getCollectionId();
			Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$addToSet", json("decks", deckId)));

			// Get the collection
			GetCollectionResponse getCollectionResponse = Inventory
					.getCollection(new GetCollectionRequest().withUserId(userId).withDeckId(createCollectionResponse.getCollectionId()));

			return DeckCreateResponse.create(deckId, getCollectionResponse);
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}

	}

	/**
	 * Processes a command to add or remove cards from a deck.
	 *
	 * @param request A possibly complex deck update command for a given user and deck ID.
	 * @return A non-null value if the deck was successfully updated.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static DeckUpdateResponse updateDeck(DeckUpdateRequest request) throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Decks/updateDeck")
				.withTag("userId", request.getUserId())
				.withTag("deckId", request.getDeckId())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			DecksUpdateCommand updateCommand = request.getUpdateCommand();
			String deckId = request.getDeckId();
			String userId = request.getUserId();
			JsonObject collectionUpdate = new JsonObject();
			List<String> added = new ArrayList<>();
			List<String> removed = new ArrayList<>();

			if (updateCommand.getSetHeroClass() != null) {
				jsonPut(collectionUpdate, "$set", json("heroClass", updateCommand.getSetHeroClass()));
			}

			if (updateCommand.getSetName() != null) {
				jsonPut(collectionUpdate, "$set", json("name", updateCommand.getSetName()));
			}

			if (!collectionUpdate.isEmpty()) {
				MongoClientUpdateResult result = mongo().updateCollection(Inventory.COLLECTIONS, json("_id", deckId), collectionUpdate);
			}

			if (updateCommand.getPullAllInventoryIds() != null
					&& !updateCommand.getPullAllInventoryIds().isEmpty()) {
				RemoveFromCollectionResponse result = Inventory.removeFromCollection(RemoveFromCollectionRequest.byInventoryIds(deckId, updateCommand.getPullAllInventoryIds()));
				removed.addAll(result.getInventoryIds());
			}

			if (updateCommand.getPushInventoryIds() != null
					&& updateCommand.getPushInventoryIds().getEach() != null
					&& !updateCommand.getPushInventoryIds().getEach().isEmpty()) {
				AddToCollectionResponse result = Inventory.addToCollection(AddToCollectionRequest.createWithInventory(deckId, updateCommand.getPushInventoryIds().getEach()));
				added.addAll(result.getInventoryIds());
			}


			if (updateCommand.getPullAllCardIds() != null
					&& !updateCommand.getPullAllCardIds().isEmpty()) {
				RemoveFromCollectionResponse result = Inventory.removeFromCollection(RemoveFromCollectionRequest.byCardIds(deckId, updateCommand.getPullAllCardIds()));
				removed.addAll(result.getInventoryIds());
			}

			if (updateCommand.getPushCardIds() != null
					&& updateCommand.getPushCardIds().getEach() != null
					&& !updateCommand.getPushCardIds().getEach().isEmpty()) {
				AddToCollectionResponse result = Inventory.addToCollection(AddToCollectionRequest.createWithCardIds(userId, deckId, updateCommand.getPushCardIds().getEach()));
				added.addAll(result.getInventoryIds());
			}

			if (updateCommand.getSetInventoryIds() != null) {
				// Remove cards, then add them back in
				List<String> inventoryIds = updateCommand.getSetInventoryIds();
				SetCollectionResponse result = Inventory.setCollection(new SetCollectionRequest(deckId, inventoryIds));
				// TODO: Populate the appropriate added/removed response here
			}
			return DeckUpdateResponse.changed(added, removed);
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}

	}

	/**
	 * Trashes a deck. When a deck is in the trash, it will not appear in the user's account but it will still exist for
	 * analytics purposes.
	 *
	 * @param request The deck to delete.
	 * @return The result of trashing the deck.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static DeckDeleteResponse deleteDeck(DeckDeleteRequest request) throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Decks/deleteDeck")
				.withTag("deckId", request.getDeckId())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			final String deckId = request.getDeckId();

			List<JsonObject> decks = mongo().findWithOptions(Inventory.COLLECTIONS,
					json("_id", deckId),
					new FindOptions().setFields(json("userId", 1)));

			final String userId = decks.get(0).getString("userId");
			// Sets the deck to trashed
			TrashCollectionResponse response = Inventory.trashCollection(new TrashCollectionRequest(deckId));

			// Remove the deckId from the user's decks
			Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId), json("$pull", json("decks", deckId)));

			return DeckDeleteResponse.create(response);
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}
}
