package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.sync.Sync;

import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

/**
 * Provides methods to manage a player's persistent inventory.
 */
public interface Inventory {
	String INVENTORY = "inventory.cards";
	String COLLECTIONS = "inventory.collections";

	/**
	 * Opens a card pack for the specified user.
	 * @param request Specifications for which sets/how many cards/how many packs should be opened for a user.
	 * @return Changes to the inventory due to this method.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	OpenCardPackResponse openCardPack(OpenCardPackRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Creates a user collection, deck collection or alliance collection with various parameters.
	 * @param request Use static methods in CreateCollectionRequest to choose from the different kinds of collections
	 *                and their arguments for creation.
	 * @return Complete information about the created collection.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	CreateCollectionResponse createCollection(CreateCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Adds a card to the specified collection. Creates inventory records for cards that are specified by ID.
	 * @param request A list of inventory records or card IDs to add to the specified collection.
	 * @return The results of adding the card to the collection. If card IDs were specified, this result will contain
	 * the inventory records of the newly created cards.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	AddToCollectionResponse addToCollection(AddToCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Removes the inventory IDs from the specified collection. Does not delete the collection nor the inventory records
	 * themselves.
	 * @param request A list of inventory IDs to remove from the collection.
	 * @return The results of the internal database update from removal.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	RemoveFromCollectionResponse removeFromCollection(RemoveFromCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Donates a card from the user's collection to the alliance's collection. Typically, when a user joins an alliance,
	 * all their cards should be donated to the alliance's collection.
	 * @param request The cards to donate.
	 * @return No additional information.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	DonateToCollectionResponse donateToCollection(DonateToCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Borrowing from a collection (typically an alliance collection) gives a user exclusive access to a card. Note,
	 * in the current design of Spellsource, cards are not exclusively borrowed so this method will not likely be used.
	 * There may be cards in the future that require exclusive access only.
	 * @param request The card to borrow.
	 * @return The number of records borrowed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	BorrowFromCollectionResponse borrowFromCollection(BorrowFromCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Returns an exclusive right to use a card. The opposite of borrowing a card from a collection.
	 * @param request The cards to return.
	 * @return The result of returning the cards.
	 */
	@Suspendable
	ReturnToCollectionResponse returnToCollection(ReturnToCollectionRequest request);

	/**
	 * Gets the complete information about a user, alliance or deck collection.
	 * @param request Use the static methods in GetCollectionRequest for the right arguments of different collection
	 *                queries.
	 * @return The complete information about a collection (user, alliance or deck).
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	GetCollectionResponse getCollection(GetCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Trashes, but does not delete, a collection.
	 * @param request The ID of the collection to trash.
	 * @return Side effects of trashing the collection.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	TrashCollectionResponse trashCollection(TrashCollectionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Sets the inventory IDs that correspond to the given collection.
	 * @param setCollectionRequest The new inventory IDs that belong to this collection.
	 * @return The collection.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	SetCollectionResponse setCollection(SetCollectionRequest setCollectionRequest) throws SuspendExecution, InterruptedException;

	@Suspendable
	static MongoClientUpdateResult update(MongoClient client, JsonObject query, JsonObject update) {
		return Sync.awaitResult(h -> client.updateCollectionWithOptions(INVENTORY, query, update, new UpdateOptions().setMulti(true), h));
	}

	@Suspendable
	static MongoClientUpdateResult update(MongoClient client, String inventoryId, JsonObject update) {
		return Sync.awaitResult(h -> client.updateCollectionWithOptions(INVENTORY, json("_id", inventoryId), update, new UpdateOptions().setMulti(true), h));
	}

	@Suspendable
	static MongoClientUpdateResult update(MongoClient client, List<String> inventoryIds, JsonObject update) {
		return Sync.awaitResult(h -> client.updateCollectionWithOptions(INVENTORY, json("_id", json("$in", inventoryIds)), update, new UpdateOptions().setMulti(true), h));
	}
}
