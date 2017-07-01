package com.hiddenswitch.minionate;

import co.paralleluniverse.fibers.SuspendExecution;
import com.google.common.io.Resources;
import com.hiddenswitch.proto3.net.DeckType;
import com.hiddenswitch.proto3.net.Inventory;
import com.hiddenswitch.proto3.net.Migrations;
import com.hiddenswitch.proto3.net.impl.*;
import com.hiddenswitch.proto3.net.models.DeckCreateRequest;
import com.hiddenswitch.proto3.net.models.DeckDeleteRequest;
import com.hiddenswitch.proto3.net.models.DeckListUpdateRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import io.vertx.core.*;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

import static com.hiddenswitch.proto3.net.util.Mongo.mongo;
import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * The Minionate Server API. Access it with {@link Minionate#minionate()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(LegacyPersistenceHandler)}.
 * <p>
 * It will provide more APIs for features in the future.
 */
public class Minionate {
	private static Minionate instance;
	List<DeckCreateRequest> cachedStandardDecks;
	Map<String, LegacyPersistenceHandler> legacyPersistenceHandlers = new HashMap<>();
	Map<String, PersistenceHandler> persistAttributeHandlers = new HashMap<>();

	private Minionate() {
	}

	/**
	 * Gets a reference to the Minionate Server API.
	 *
	 * @return An API instance.
	 */
	public static Minionate minionate() {
		if (instance == null) {
			instance = new Minionate();
		}

		return instance;
	}

	/**
	 * The common migration for a given Minionate cluster.
	 *
	 * @param vertx The vertx instance.
	 * @param then  A handler when the migration that tells you if it was or was not successful.
	 * @return
	 */
	public Minionate migrate(Vertx vertx, Handler<AsyncResult<Void>> then) {
		mongo().connectWithEnvironment(vertx);

		Migrations.migrate(vertx)
				.add(new MigrationRequest()
						.withVersion(1)
						.withUp(thisVertx -> {
							try {
								mongo().createIndex(Inventory.COLLECTIONS, json("deckType", 1));
							} catch (Throwable ignored) {
							}

							// All draft decks should have the draft flag set
							MongoClientUpdateResult u1 = mongo().updateCollectionWithOptions(Inventory.COLLECTIONS,
									json("name", json("$regex", "Draft Deck")),
									json("$set", json("deckType", DeckType.DRAFT.toString())),
									new UpdateOptions().setMulti(true));

							// All other decks should have the constructed flag
							MongoClientUpdateResult u2 = mongo().updateCollectionWithOptions(Inventory.COLLECTIONS,
									json("deckType", json("$ne", DeckType.DRAFT.toString())),
									json("$set", json("deckType", DeckType.CONSTRUCTED.toString())),
									new UpdateOptions().setMulti(true));

							// Update to the latest decklist
							InventoryImpl inventory = new InventoryImpl();
							CardsImpl cards = new CardsImpl();
							DecksImpl decksImpl = new DecksImpl();
							String deploymentId = awaitResult(h -> thisVertx.deployVerticle(decksImpl, h));
							String deploymentId2 = awaitResult(h -> thisVertx.deployVerticle(inventory, h));
							String deploymentId3 = awaitResult(h -> thisVertx.deployVerticle(cards, h));
							decksImpl.updateAllDecks(new DeckListUpdateRequest()
									.withDeckCreateRequests(Minionate.minionate().getStandardDecks()));
							Void ignored = awaitResult(h -> thisVertx.undeploy(deploymentId, h));
							ignored = awaitResult(h -> thisVertx.undeploy(deploymentId2, h));
							ignored = awaitResult(h -> thisVertx.undeploy(deploymentId3, h));
						}))
				.add(new MigrationRequest()
						.withVersion(2)
						.withUp(thisVertx -> {
							InventoryImpl inventory = new InventoryImpl();
							CardsImpl cards = new CardsImpl();
							DecksImpl decksImpl = new DecksImpl();
							String deploymentId = awaitResult(h -> thisVertx.deployVerticle(decksImpl, h));
							String deploymentId2 = awaitResult(h -> thisVertx.deployVerticle(inventory, h));
							String deploymentId3 = awaitResult(h -> thisVertx.deployVerticle(cards, h));
							// Trash the druid deck
							List<String> deckIds = mongo().findWithOptions(Inventory.COLLECTIONS,
									json("name", json("$regex", "Ramp Combo Druid")),
									new FindOptions().setFields(json("_id", true))).stream()
									.map(o -> o.getString("_id")).collect(toList());
							for (String deckId : deckIds) {
								decksImpl.deleteDeck(new DeckDeleteRequest(deckId));
							}
							Void ignored = awaitResult(h -> thisVertx.undeploy(deploymentId, h));
							ignored = awaitResult(h -> thisVertx.undeploy(deploymentId2, h));
							ignored = awaitResult(h -> thisVertx.undeploy(deploymentId3, h));
						}))
				.migrateTo(2, then2 ->
						then.handle(then2.succeeded() ? Future.succeededFuture() : Future.failedFuture(then2.cause())));
		return this;
	}

	/**
	 * Gets the current deck lists specified in the decklists.current resources directory.
	 *
	 * @return A list of deck create requests without a {@link DeckCreateRequest#userId} specified.
	 */
	public List<DeckCreateRequest> getStandardDecks() {
		if (cachedStandardDecks == null) {
			Reflections reflections = new Reflections("decklists.current", new ResourcesScanner());
			Set<URL> resourceList = reflections.getResources(x -> true).stream().map(Resources::getResource).collect(toSet());
			cachedStandardDecks = resourceList.stream().map(c -> {
				try {
					return Resources.toString(c, Charset.defaultCharset());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}).map((deckList) -> {
				try {
					return DeckCreateRequest.fromDeckList(deckList);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).filter(c -> null != c).collect(toList());
		}

		return cachedStandardDecks;
	}

	/**
	 * Persist an attribute when the given game event occurs, using the provided handler to compute the new value and
	 * to persist it with a {@link PersistenceContext#update(EntityReference, Object)} call inside the handler.
	 * <p>
	 * For example, let's say we want to persist the total amount of damage a minion has dealt:
	 * <pre>
	 *     {@code
	 *     		Minionate.minionate().persistAttribute(
	 *              "total-damage-dealt-1",
	 *              GameEventType.AFTER_PHYSICAL_ATTACK,
	 *              Attribute.TOTAL_DAMAGE_DEALT,
	 *              (PersistenceContext<AfterPhysicalAttackEvent> context) -> {
	 *                  int attackerDamage = context.event().getDamageDealt();
	 *                  context.update(context.event().getAttacker().getReference(), attackerDamage);
	 *              }
	 *          );
	 *     }
	 * </pre>
	 *
	 * @param id        A name of your choosing to uniquely identify this persistence handler.
	 * @param event     The type of event that this handler should be triggered for.
	 * @param attribute The attribute this handler will be persisting.
	 * @param handler   A handler that is passed a {@link PersistenceContext}, whose methods provide the event and a
	 *                  mechanism to update the entity with a new attribute value (both in the {@link GameContext} where
	 *                  this event is currently taking place and in the entity's corresponding {@link
	 *                  com.hiddenswitch.proto3.net.impl.util.InventoryRecord} where the value will be persisted in a
	 *                  database.
	 * @param <T>       The type of the event that corresponds to the provided {@link GameEventType}.
	 */
	public <T extends GameEvent> void persistAttribute(String id, GameEventType event, Attribute attribute, Handler<PersistenceContext<T>> handler) {
		persistAttributeHandlers.put(id, new PersistenceHandler<>(Sync.fiberHandler(handler), id, event, attribute));
	}

	/**
	 * @param legacyHandler A handler for game events and logic requests. See {@link LegacyPersistenceHandler#create(String,
	 *                      GameEventType, Function, Function)} for an easy way to create this handler.
	 * @param <T>           The event type.
	 */
	public <T extends GameEvent> void persistAttribute(LegacyPersistenceHandler<T> legacyHandler) {
		legacyPersistenceHandlers.put(legacyHandler.getId(), legacyHandler);
	}

	/**
	 * Deploys all the services needed to run an embedded server.
	 *
	 * @param vertx       A vertx instance.
	 * @param deployments A handler for the successful deployments. If any deployment fails, the entire handler fails.
	 */
	public void deployAll(Vertx vertx, Handler<AsyncResult<CompositeFuture>> deployments) {
		final List<SyncVerticle> verticles = Arrays.asList(new SyncVerticle[]{
				new CardsImpl(),
				new AccountsImpl(),
				new GamesImpl(),
				new MatchmakingImpl(),
				new BotsImpl(),
				new LogicImpl(),
				new DecksImpl(),
				new InventoryImpl(),
				new DraftImpl(),
				new GatewayImpl()});

		CompositeFuture.all(verticles.stream().map(verticle -> {
			final Future<String> future = Future.future();
			vertx.deployVerticle(verticle, future);
			return future;
		}).collect(toList())).setHandler(deployments);
	}

	/**
	 * A sync version of {@link #deployAll(Vertx, Handler)}.
	 *
	 * @param vertx A vertx instance.
	 * @return The result. Failed if any deployment failed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	public CompositeFuture deployAll(Vertx vertx) throws SuspendExecution, InterruptedException {
		return Sync.awaitResult(h -> deployAll(vertx, h));
	}

	/**
	 * Access non-client features required to implement the persistence features.
	 *
	 * @return A {@link Persistence} utility.
	 */
	public Persistence persistence() {
		return new Persistence(this);
	}

}
