package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.io.Resources;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.mongo.WriteOption;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.Router;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;
import org.apache.commons.lang3.RandomStringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static com.hiddenswitch.spellsource.Draft.DRAFTS;
import static com.hiddenswitch.spellsource.Inventory.COLLECTIONS;
import static com.hiddenswitch.spellsource.Inventory.INVENTORY;
import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * The Spellsource Server API. Access it with {@link Spellsource#spellsource()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(String,
 * GameEventType, Attribute, Handler)}.
 * <p>
 * It will provide more APIs for features in the future.
 */
public class Spellsource {
	private static Logger logger = LoggerFactory.getLogger(Spellsource.class);
	private static Spellsource instance;
	private List<DeckCreateRequest> cachedStandardDecks;
	private Map<String, PersistenceHandler> persistAttributeHandlers = new HashMap<>();
	private Map<String, Trigger> gameTriggers = new HashMap<>();
	private Map<String, Spell> spells = new HashMap<>();

	private Spellsource() {
	}

	/**
	 * Gets a reference to the Spellsource Server API.
	 *
	 * @return An API instance.
	 */
	public synchronized static Spellsource spellsource() {
		if (instance == null) {
			instance = new Spellsource();
		}

		return instance;
	}

	/**
	 * The common migration for a given Spellsource cluster.
	 *
	 * @param vertx The vertx instance.
	 * @param then  A handler when the migration that tells you if it was or was not successful.
	 * @return
	 */
	public Spellsource migrate(Vertx vertx, Handler<AsyncResult<Void>> then) {
		mongo().connectWithEnvironment(vertx);

		Migrations.migrate(vertx)
				.add(new MigrationRequest()
						.withVersion(1)
						.withUp(thisVertx -> {
							final List<String> collections = mongo().getCollections();
							try {
								if (!collections.contains(Accounts.USERS)) {
									mongo().createCollection(Accounts.USERS);
								}

								if (!collections.contains(INVENTORY)) {
									mongo().createCollection(INVENTORY);
								}

								if (!collections.contains(COLLECTIONS)) {
									mongo().createCollection(COLLECTIONS);
								}

								if (!collections.contains(DRAFTS)) {
									mongo().createCollection(DRAFTS);
								}
							} finally {
								logger.info("add MigrationRequest 11: Collections created added if possible");
							}


							try {
								mongo().createIndex(Accounts.USERS, json(UserRecord.EMAILS_ADDRESS, 1));
								mongo().createIndex(INVENTORY, json("userId", 1));
								mongo().createIndex(INVENTORY, json("collectionIds", 1));
								mongo().createIndex(INVENTORY, json("cardDesc.id", 1));
							} finally {
								logger.info("add MigrationRequest 11: Indices added if possible");
							}


						}))
				.add(new MigrationRequest()
						.withVersion(2)
						.withUp(thisVertx -> {
							try {
								mongo().createIndex(COLLECTIONS, json("deckType", 1));
							} catch (Throwable ignored) {
							}

							// All draft decks should have the draft flag set
							MongoClientUpdateResult u1 = mongo().updateCollectionWithOptions(COLLECTIONS,
									json("name", json("$regex", "Draft Deck")),
									json("$set", json("deckType", DeckType.DRAFT.toString())),
									new UpdateOptions().setMulti(true));

							// All other decks should have the constructed flag
							MongoClientUpdateResult u2 = mongo().updateCollectionWithOptions(COLLECTIONS,
									json("deckType", json("$ne", DeckType.DRAFT.toString()),
											"type", CollectionTypes.DECK.toString()),
									json("$set", json("deckType", DeckType.CONSTRUCTED.toString())),
									new UpdateOptions().setMulti(true));

							// Update to the latest decklist

							Decks.updateAllDecks(new DeckListUpdateRequest()
									.withDeckCreateRequests(Spellsource.spellsource().getStandardDecks()));
						}))
				.add(new MigrationRequest()
						.withVersion(3)
						.withUp(thisVertx -> {
							// Trash the druid deck
							List<String> deckIds = mongo().findWithOptions(COLLECTIONS,
									json("name", json("$regex", "Ramp Combo Druid")),
									new FindOptions().setFields(json("_id", true))).stream()
									.map(o -> o.getString("_id")).collect(toList());
							for (String deckId : deckIds) {
								Decks.deleteDeck(DeckDeleteRequest.create(deckId));
							}
						}))
				.add(new MigrationRequest()
						.withVersion(4)
						.withUp(thisVertx -> {
							// Repair user collections
							Mongo.mongo().updateCollectionWithOptions(COLLECTIONS, json("heroClass", json("$eq", null)),
									json("$unset", json("deckType", 1), "$set", json("trashed", false)), new UpdateOptions().setMulti(true));

							for (JsonObject record : Mongo.mongo().findWithOptions(Accounts.USERS, json(), new FindOptions().setFields(json("_id", 1)))) {
								final String userId = record.getString("_id");
								Mongo.mongo().updateCollectionWithOptions(INVENTORY, json("userId", userId), json("$addToSet", json("collectionIds", userId)), new UpdateOptions().setMulti(true));
							}

							// Remove all inventory records that are in just one collection, the user collection
							Mongo.mongo().removeDocuments(INVENTORY, json("collectionIds", json("$size", 1)));
						}))
				.add(new MigrationRequest()
						.withVersion(5)
						.withUp(thisVertx -> {
							// Set all existing decks to standard.
							Mongo.mongo().updateCollectionWithOptions(COLLECTIONS,
									json("format", json("$exists", false)),
									json("$set", json("format", "Standard")),
									new UpdateOptions().setMulti(true));
						})
						.withDown(thisVertx -> {
							// Remove format field
							Mongo.mongo().updateCollectionWithOptions(COLLECTIONS,
									json("format", json("$exists", true)),
									json("$unset", json("format", null)),
									new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(6)
						.withUp(thisVertx -> {
							// Shuffle around the location of user record data
							List<JsonObject> users = Mongo.mongo().find(Accounts.USERS, json());
							for (JsonObject jo : users) {
								if (!jo.containsKey("profile")
										|| !jo.getJsonObject("profile").containsKey("emailAddress")) {
									continue;
								}

								String email = jo.getJsonObject("profile").getString("emailAddress");
								String username = jo.getJsonObject("profile").getString("displayName");
								String passwordScrypt = jo.getJsonObject("auth").getString("scrypt");

								EmailRecord emailRecord = new EmailRecord();
								emailRecord.setAddress(email);

								List<JsonObject> tokens = jo.getJsonObject("auth").getJsonArray("tokens").stream()
										.map(e -> (JsonObject) e)
										.map(old -> {
											HashedLoginTokenRecord newToken = new HashedLoginTokenRecord();
											newToken.setHashedToken(old.getString("hashedLoginToken"));
											newToken.setWhen(LoginToken.expiration());
											return json(newToken);
										}).collect(toList());

								JsonObject updateCommand = json(
										"$set", json(
												"emails", Collections.singletonList(json(emailRecord)),
												"username", username,
												"services", json(
														"password", json(
																"scrypt", passwordScrypt
														),
														"resume", json(
																"loginTokens", tokens
														)
												)),
										"$unset", json("auth", null, "profile", null)
								);

								String userId = jo.getString("_id");
								logger.debug("add MigrationRequest 5: Migrating passwords and emails for userId {}", userId);

								Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId),
										updateCommand);
							}
						}))
				.add(new MigrationRequest()
						.withVersion(7)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							MongoClientUpdateResult result1 = changeCardId("spell_temporary_anomaly", "spell_temporal_anomaly");
							MongoClientUpdateResult result2 = changeCardId("minion_doomlord", "minion_dreadlord");
							logger.info("add MigrationRequest 6: Fixed {} Temporal Anomaly cards, {} Dreadlord cards", result1.getDocModified(), result2.getDocModified());
						}))
				.add(new MigrationRequest()
						.withVersion(8)
						.withUp(thisVertx -> {
							// Creates an index on the cardDesc.id property to help find cards in inventory management
							mongo().createIndex(INVENTORY, json("cardDesc.id", 1));
						}))
				.add(new MigrationRequest()
						.withVersion(9)
						.withUp(thisVertx -> {
							// Remove all fields except the cardDesc.id field
							mongo().updateCollectionWithOptions(INVENTORY, json(), json(
									"$unset", json(
											"cardDesc.name", 1,
											"cardDesc.description", 1,
											"cardDesc.legacy", 1,
											"cardDesc.type", 1,
											"cardDesc.heroClass", 1,
											"cardDesc.heroClasses", 1,
											"cardDesc.rarity", 1,
											"cardDesc.set", 1,
											"cardDesc.baseManaCost", 1,
											"cardDesc.collectible", 1,
											"cardDesc.attributes", 1,
											"cardDesc.fileFormatVersion", 1,
											"cardDesc.manaCostModifier", 1,
											"cardDesc.passiveTriggers", 1,
											"cardDesc.deckTrigger", 1,
											"cardDesc.gameTriggers", 1,
											"cardDesc.battlecry", 1,
											"cardDesc.deathrattle", 1,
											"cardDesc.trigger", 1,
											"cardDesc.triggers", 1,
											"cardDesc.aura", 1,
											"cardDesc.auras", 1,
											"cardDesc.race", 1,
											"cardDesc.cardCostModifier", 1,
											"cardDesc.baseAttack", 1,
											"cardDesc.baseHp", 1,
											"cardDesc.damage", 1,
											"cardDesc.durability", 1,
											"cardDesc.onEquip", 1,
											"cardDesc.onUnequip", 1,
											"cardDesc.heroPower", 1,
											"cardDesc.targetSelection", 1,
											"cardDesc.spell", 1,
											"cardDesc.condition", 1,
											"cardDesc.group", 1,
											"cardDesc.secret", 1,
											"cardDesc.quest", 1,
											"cardDesc.countUntilCast", 1,
											"cardDesc.options", 1,
											"cardDesc.bothOptions", 1
									)
							), new UpdateOptions().setMulti(true).setWriteOption(WriteOption.UNACKNOWLEDGED));
						}))
				.add(new MigrationRequest()
						.withVersion(10)
						.withUp(thisVertx -> {
							// Refresh the bot decks
							List<JsonObject> bots = mongo().findWithOptions(Accounts.USERS, json("bot", true), new FindOptions().setFields(json("decks", 1)));
							for (JsonObject bot : bots) {
								for (Object obj : bot.getJsonArray("decks")) {
									String deckId = (String) obj;
									Decks.deleteDeck(DeckDeleteRequest.create(deckId));
								}
								for (DeckCreateRequest req : Spellsource.spellsource().getStandardDecks()) {
									if (req.getFormat().equals("Standard")) {
										Decks.createDeck(req.withUserId(bot.getString("_id")));
									}
								}
							}
						}))
				.add(new MigrationRequest()
						.withVersion(11)
						.withUp(thisVertx -> {
							// Add an index for the friend IDs
							mongo().createIndex(Accounts.USERS, json("friends.friendId", 1));
						}))
				.add(new MigrationRequest()
						.withVersion(12)
						.withUp(thisVertx -> {
							// Give all users a privacy token
							for (JsonObject userRecord : mongo().find(Accounts.USERS, json())) {
								mongo().updateCollection(Accounts.USERS, json("_id", userRecord.getString("_id")),
										json("$set", json("privacyToken", RandomStringUtils.randomNumeric(4))));
							}


							// Add an index for invites
							if (!mongo().getCollections().contains(Invites.INVITES)) {
								mongo().createCollection(Invites.INVITES);
							}

							mongo().createIndex(Invites.INVITES, json("toUserId", 1));
							// Create an index for the username
							mongo().createIndex(Accounts.USERS, json("username", 1));
						}))
				.add(new MigrationRequest()
						.withVersion(13)
						.withUp(thisVertx -> {
							// Remove all bot accounts.
							long botsRemoved = Accounts.removeAccounts(mongo().find(Accounts.USERS, json("bot", true)).stream().map(jo -> new UserId(jo.getString("_id"))).collect(toList()));
							logger.info("add MigrationRequest 13: Removed {} bot accounts", botsRemoved);
						}))
				.add(new MigrationRequest()
						.withVersion(14)
						.withUp(thisVertx -> {
							changeCardId("minion_diabologist", "minion_frenzied_diabolist");
						}))
				.add(new MigrationRequest()
						.withVersion(15)
						.withUp(thisVertx -> {
							changeCardId("token_pumpkin_peasant", "minion_pumpkin_peasant");
						}))
				.migrateTo(15, then2 ->
						then.handle(then2.succeeded() ? Future.succeededFuture() : Future.failedFuture(then2.cause())));
		return this;
	}

	/**
	 * Gets the current deck lists specified in the decklists.current resources directory.
	 *
	 * @return A list of deck create requests without a {@link DeckCreateRequest#userId} specified.
	 */
	public synchronized List<DeckCreateRequest> getStandardDecks() {
		if (cachedStandardDecks == null) {
			CardCatalogue.loadCardsFromPackage();
			cachedStandardDecks = new ArrayList<>();
			Reflections reflections = new Reflections("decklists.current", new ResourcesScanner());
			Set<URL> resourceList = reflections.getResources(x -> true).stream().map(Resources::getResource).collect(toSet());
			cachedStandardDecks.addAll(resourceList.stream().map(c -> {
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
			}).filter(Objects::nonNull).collect(toList()));
		}

		return cachedStandardDecks;
	}

	/**
	 * Persist an attribute when the given game event occurs, using the provided handler to compute the new value and to
	 * persist it with a {@link PersistenceContext#update(EntityReference, Object)} call inside the handler.
	 * <p>
	 * For example, let's say we want to persist the total amount of damage a minion has dealt:
	 * <pre>
	 *     {@code
	 *     		Spellsource.Spellsource().persistAttribute(
	 *              "total-damage-dealt-1",
	 *              GameEventType.AFTER_PHYSICAL_ATTACK,
	 *              Attribute.LIFETIME_DAMAGE_DEALT,
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
	 *                  this event is currently taking place and in the entity's corresponding {@link InventoryRecord}
	 *                  where the value will be persisted in a database.
	 * @param <T>       The type of the event that corresponds to the provided {@link GameEventType}.
	 */
	public <T extends GameEvent> Spellsource persistAttribute(String id, GameEventType event, Attribute attribute, Handler<PersistenceContext<T>> handler) {
		getPersistAttributeHandlers().put(id, new PersistenceHandler<>(Sync.fiberHandler(handler), id, event, attribute));
		return this;
	}

	/**
	 * Configures a trigger to be added to the start of every game.
	 *
	 * @param id               An ID for this trigger.
	 * @param eventTriggerDesc The event this trigger should listen for.
	 * @param spell            The spell that should be casted by this event trigger desc.
	 * @return This Spellsource instance.
	 */
	public Spellsource trigger(String id, EventTriggerDesc eventTriggerDesc, Spell spell) {
		getSpells().put(id, spell);
		getGameTriggers().put(id, new Trigger(eventTriggerDesc, id));
		return this;
	}

	/**
	 * Deploys all the services needed to run an embedded server.
	 *
	 * @param vertx       A vertx instance.
	 * @param deployments A handler for the successful deployments. If any deployment fails, the entire handler fails.
	 */
	public void deployAll(Vertx vertx, Handler<AsyncResult<CompositeFuture>> deployments) {
		final List<Verticle> verticles = Arrays.asList(services());

		CompositeFuture.all(verticles.stream().map(verticle -> {
			final Future<String> future = Future.future();
			vertx.deployVerticle(verticle, future);
			return future;
		}).collect(toList())).setHandler(deployments);
	}

	protected Verticle[] services() {
		return new Verticle[]{
				Games.create(),
				Gateway.create()};
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

	public Map<String, PersistenceHandler> getPersistAttributeHandlers() {
		return persistAttributeHandlers;
	}

	public void close() {
		instance = null;
	}

	public Map<String, Trigger> getGameTriggers() {
		return gameTriggers;
	}

	public Map<String, Spell> getSpells() {
		return spells;
	}

	@Suspendable
	public static MongoClientUpdateResult changeCardId(String oldId, String newId) {
		if (CardCatalogue.getCardById(newId) == null) {
			logger.error("changeCardId: Cannot change {} to {} because the new ID does not exist", oldId, newId);
			return new MongoClientUpdateResult();
		}

		return Mongo.mongo().updateCollectionWithOptions(INVENTORY,
				json("cardDesc.id", oldId), json("$set", json("cardDesc.id", newId)), new UpdateOptions().setMulti(true));
	}
}
