package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import com.google.common.io.Resources;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.Trigger;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.models.CollectionTypes;
import com.hiddenswitch.spellsource.models.DeckDeleteRequest;
import com.hiddenswitch.spellsource.models.DeckListUpdateRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.util.Mongo;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
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
import static com.hiddenswitch.spellsource.util.QuickJson.array;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * The Spellsource Server API. Access it with {@link Spellsource#spellsource()}.
 * <p>
 * This class provides an easy way to provide a new persist attribute with {@link #persistAttribute(String,
 * GameEventType, Attribute, SuspendableAction1)}.
 * <p>
 * It will provide more APIs for features in the future.
 * <p>
 * When adding new collections, this class stores the migrations where index creation is appropriate.
 *
 * @see com.hiddenswitch.spellsource.applications.LocalClustered for the entry point of the executable.
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
								logger.info("add MigrationRequest 1: Collections created added if possible");
							}


							try {
								mongo().createIndex(Accounts.USERS, json(UserRecord.EMAILS_ADDRESS, 1));
								mongo().createIndex(INVENTORY, json("userId", 1));
								mongo().createIndex(INVENTORY, json("collectionIds", 1));
								mongo().createIndex(INVENTORY, json("cardDesc.id", 1));
							} finally {
								logger.info("add MigrationRequest 1: Indices added if possible");
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
								logger.debug("add MigrationRequest 6: Migrating passwords and emails for userId {}", userId);

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
							logger.info("add MigrationRequest 7: Fixed {} Temporal Anomaly cards, {} Dreadlord cards", result1.getDocModified(), result2.getDocModified());
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
							Bots.updateBotDeckList();
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
							CardCatalogue.loadCardsFromPackage();
							changeCardId("minion_diabologist", "minion_frenzied_diabolist");
						}))
				.add(new MigrationRequest()
						.withVersion(15)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							changeCardId("token_pumpkin_peasant", "minion_pumpkin_peasant");
						}))
				.add(new MigrationRequest()
						.withVersion(16)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							removeCards("spell_forbidden_evolution", "minion_seadevil_totem", "minion_tactician",
									"spell_last_stand", "spell_starsurge", "minion_dranghul");
						}))
				.add(new MigrationRequest()
						.withVersion(17)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							removeCards("minion_shadowglen_vagrant", "minion_lone_wolf", "spell_bag_of_tricks");
						}))
				.add(new MigrationRequest()
						.withVersion(18)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							removeCards("spell_lone_wolf");
						}))
				.add(new MigrationRequest()
						.withVersion(19)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							changeCardId("spell_eerie_fermentation", "quest_eerie_fermentation");
						}))
				.add(new MigrationRequest()
						.withVersion(20)
						.withUp(thisVertx -> {
							mongo().updateCollectionWithOptions(Inventory.COLLECTIONS, json(), json("$set", json(
									"wins", 0, "totalGames", 0
							)), new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(21)
						.withUp(thisVertx -> {
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(22)
						.withUp(thisVertx -> {
							// Needs to include the fromUserId too
							mongo().createIndex(Invites.INVITES, json("fromUserId", 1));
						}))
				.add(new MigrationRequest()
						.withVersion(23)
						.withUp(thisVertx -> {
							// Remove all cards that don't exist
							CardCatalogue.loadCardsFromPackage();
							JsonArray allCardIds = array(CardCatalogue.getRecords().keySet().toArray());
							MongoClientDeleteResult removed = Mongo.mongo().removeDocuments(INVENTORY, json("cardDesc.id",
									json("$nin", allCardIds)));
							logger.info("add MigrationRequest 23: Removed {} cards that no longer exist", removed.getRemovedCount());
						}))
				.add(new MigrationRequest()
						.withVersion(24)
						.withUp(thisVertx -> {
							mongo().createCollection(Games.GAMES);
							mongo().createIndex(Games.GAMES, json(GameRecord.PLAYER_USER_IDS, 1));
						}))
				.add(new MigrationRequest()
						.withVersion(25)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							changeCardId("spell_lesser_oynx_spellstone", "spell_lesser_onyx_spellstone");
						}))
				.add(new MigrationRequest()
						.withVersion(26)
						.withUp(thisVertx -> {
							// DoctorPangloss is the first and only admin user
							mongo().updateCollectionWithOptions(Accounts.USERS, json(), json("$set", json(UserRecord.ROLES, array())), new UpdateOptions().setMulti(true));
							MongoClientUpdateResult res = mongo().updateCollection(Accounts.USERS, json(UserRecord.EMAILS_ADDRESS, "benjamin.s.berman@gmail.com"), json("$addToSet", json(UserRecord.ROLES, Accounts.Authorities.ADMINISTRATIVE.name())));
							logger.info("add MigrationRequest 26: {} users made administrators", res.getDocModified());
						}))
				.add(new MigrationRequest()
						.withVersion(27)
						.withUp(thisVertx -> {
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(28)
						.withUp(thisVertx -> {
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(29)
						.withUp(thisVertx -> {
							changeCardId("minon_treeleach", "minion_treeleach");
						}))
				.add(new MigrationRequest()
						.withVersion(30)
						.withUp(thisVertx -> {
							changeCardId("minion_anub'rekhan", "minion_anobii");
							changeCardId("minion_azjol_visionary", "minion_visionary");
							changeCardId("minion_nerubian_vizier", "minion_vizier");
							changeCardId("weapon_maexxnas_femur", "weapon_scepter_of_bees");
							changeCardId("minion_qiraji_guardian", "minion_grand_guardian");
							changeCardId("minion_prophet_skeram", "minion_vermancer_prophet");
							changeCardId("minion_silithid_wasp", "minion_servant_wasp");
							changeCardId("spell_elementium_shell", "spell_reinforced_shell");
						}))
				.add(new MigrationRequest()
						.withVersion(31)
						.withUp(thisVertex -> {
							changeCardId("minion_student_of_the_tiger", "minion_lungrath_hunter");
						}))
				.add(new MigrationRequest()
						.withVersion(32)
						.withUp(thisVertex -> {
							changeCardId("hero_chen_stormstout", "hero_mienzhou");
						}))
				.add(new MigrationRequest()
						.withVersion(33)
						.withUp(thisVertex -> {
							changeCardId("hero_power_meditation", "hero_power_effuse");
						}))
				.add(new MigrationRequest()
						.withVersion(34)
						.withUp(thisVertex -> {
							changeCardId("minion_blessed_koi_statue", "minion_jade_serpent_statue");
						}))
				.add(new MigrationRequest()
						.withVersion(35)
						.withUp(thisVertex -> {
							changeCardId("minion_skunky_brew_alemental", "minion_deepwoods_elemental");
						}))
				.add(new MigrationRequest()
						.withVersion(36)
						.withUp(thisVertex -> {
							changeCardId("minion_crane_school_instructor", "minion_desciple_of_shitakiri");
						}))
				.add(new MigrationRequest()
						.withVersion(37)
						.withUp(thisVertex -> {
							changeCardId("minion_black_ox_statue", "minion_enchanted_tapestry");
						}))
				.add(new MigrationRequest()
						.withVersion(38)
						.withUp(thisVertex -> {
							changeCardId("minion_emperor_shaohao", "minion_master_jigen");
						}))
				.add(new MigrationRequest()
						.withVersion(39)
						.withUp(thisVertex -> {
							changeCardId("minion_monastery_guard", "minion_monastery_warden");
						}))
				.add(new MigrationRequest()
						.withVersion(40)
						.withUp(thisVertex -> {
							changeCardId("minion_windwalk_master", "minion_shigaraki_elder");
						}))
				.add(new MigrationRequest()
						.withVersion(41)
						.withUp(thisVertex -> {
							changeCardId("minion_elusive_brawler", "minion_sly_brawler");
						}))
				.add(new MigrationRequest()
						.withVersion(42)
						.withUp(thisVertex -> {
							changeCardId("spell_leg_sweep", "spell_axe_kick");
						}))
				.add(new MigrationRequest()
						.withVersion(43)
						.withUp(thisVertex -> {
							changeCardId("spell_chi_torpedo", "spell_chi_restoration");
						}))
				.add(new MigrationRequest()
						.withVersion(45)
						.withUp(thisVertex -> {
							changeCardId("spell_gift_of_the_mists", "spell_enlightenment");
						}))
				.add(new MigrationRequest()
						.withVersion(46)
						.withUp(thisVertex -> {
							changeCardId("spell_flying_serpent_kick", "spell_fiery_kitsune_punch");
						}))
				.add(new MigrationRequest()
						.withVersion(47)
						.withUp(thisVertex -> {
							changeCardId("spell_fortifying_brew", "spell_fortifying_prayer");
						}))
				.add(new MigrationRequest()
						.withVersion(48)
						.withUp(thisVertex -> {
							changeCardId("spell_storm_earth_and_fire", "spell_fury_of_the_elements");
						}))
				.add(new MigrationRequest()
						.withVersion(49)
						.withUp(thisVertex -> {
							changeCardId("spell_staggering_brew", "spell_honed_potion");
						}))
				.add(new MigrationRequest()
						.withVersion(50)
						.withUp(thisVertex -> {
							changeCardId("spell_keg_smash", "spell_mark_of_despair");
						}))
				.add(new MigrationRequest()
						.withVersion(51)
						.withUp(thisVertex -> {
							changeCardId("spell_tiger_palm_strike", "spell_palm_strike");
						}))
				.add(new MigrationRequest()
						.withVersion(52)
						.withUp(thisVertex -> {
							changeCardId("spell_effuse", "spell_springs_of_ebisu");
						}))
				.add(new MigrationRequest()
						.withVersion(53)
						.withUp(thisVertex -> {
							changeCardId("spell_dampen_harm", "spell_steadfast_defense");
						}))
				.add(new MigrationRequest()
						.withVersion(54)
						.withUp(thisVertex -> {
							changeCardId("spell_breath_of_fire", "spell_windswept_strike");
						}))
				.add(new MigrationRequest()
						.withVersion(55)
						.withUp(thisVertex -> {
							changeCardId("spell_storm_spirit", "spell_bellowing_spirit");
						}))
				.add(new MigrationRequest()
						.withVersion(56)
						.withUp(thisVertex -> {
							changeCardId("spell_fire_spirit", "spell_burning_spirit");
						}))
				.add(new MigrationRequest()
						.withVersion(57)
						.withUp(thisVertex -> {
							changeCardId("token_xuen_the_white_tiger", "token_kumiho_nine_tailed_kitsune");
						}))
				.add(new MigrationRequest()
						.withVersion(58)
						.withUp(thisVertex -> {
							changeCardId("token_chi_ji_the_red_crane", "token_shitakiri_slit_tongue_suzume");
						}))
				.add(new MigrationRequest()
						.withVersion(59)
						.withUp(thisVertex -> {
							changeCardId("token_tiny_alemental", "token_stony_elemental");
						}))
				.add(new MigrationRequest()
						.withVersion(60)
						.withUp(thisVertex -> {
							changeCardId("token_earth_spirit", "token_unearthed_spirit");
						}))
				.add(new MigrationRequest()
						.withVersion(61)
						.withUp(thisVertex -> {
							changeCardId("token_niuzao_the_black_ox", "token_yashima_cheerful_tanuki");
						}))
				.migrateTo(61, then2 ->
						then.handle(then2.succeeded() ? Future.succeededFuture() : Future.failedFuture(then2.cause())));
		return this;
	}

	/**
	 * Gets the current deck lists specified in the decklists.current resources directory.
	 *
	 * @return A list of deck create requests without a {@link DeckCreateRequest#getUserId()} specified.
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
			}).map((deckList) -> DeckCreateRequest.fromDeckList(deckList).setStandardDeck(true)).filter(Objects::nonNull).collect(toList()));
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
	public <T extends GameEvent> Spellsource persistAttribute(String id, GameEventType event, Attribute attribute, SuspendableAction1<PersistenceContext<T>> handler) {
		if (getPersistAttributeHandlers().containsKey(id)) {
			return this;
		}
		getPersistAttributeHandlers().put(id, new PersistenceHandler<>(suspendableHandler(handler), id, event, attribute));
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
	@Suspendable
	public void deployAll(Vertx vertx, Handler<AsyncResult<CompositeFuture>> deployments) {
		List<Future> futures = new ArrayList<>();
		// Use up all the event loops
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
			for (Verticle verticle : services()) {
				final Future<String> future = Future.future();
				vertx.deployVerticle(verticle, future);
				futures.add(future);
			}

		CompositeFuture.all(futures).setHandler(deployments);
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
	protected static MongoClientUpdateResult changeCardId(String oldId, String newId) {
		try {
			CardCatalogue.getCardById(newId);
		} catch (Throwable any) {
			logger.error("changeCardId: Cannot change {} to {} because the new ID does not exist", oldId, newId);
			return new MongoClientUpdateResult();
		}

		return Mongo.mongo().updateCollectionWithOptions(INVENTORY,
				json("cardDesc.id", oldId), json("$set", json("cardDesc.id", newId)), new UpdateOptions().setMulti(true));
	}

	@Suspendable
	protected static MongoClientDeleteResult removeCards(String... ids) {
		return Mongo.mongo().removeDocuments(INVENTORY, json("cardDesc.id",
				json("$in", array(ids))));
	}
}
