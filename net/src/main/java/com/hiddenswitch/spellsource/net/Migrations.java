package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.cards.base.BaseCardResources;
import com.hiddenswitch.spellsource.net.impl.*;
import com.hiddenswitch.spellsource.net.impl.util.*;
import com.hiddenswitch.spellsource.net.models.*;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.spellsource.net.Draft.DRAFTS;
import static com.hiddenswitch.spellsource.net.Inventory.COLLECTIONS;
import static com.hiddenswitch.spellsource.net.Inventory.INVENTORY;
import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.array;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static java.util.stream.Collectors.toList;

/**
 * The migrations service performs database migrations on Mongo, using a mongo document as a lock.
 */
public interface Migrations extends Verticle {
	static Logger LOGGER = LoggerFactory.getLogger(Migrations.class);

	/**
	 * The common migration for a given Spellsource cluster.
	 * <p>
	 * Migrations will only run on one instance in the cluster at a time.
	 * <p>
	 * Failing a migration will prevent the server from deploying (leaving it in an unhealthy state).
	 *
	 * @param vertx The vertx instance.
	 * @param then  A handler when the migration that tells you if it was or was not successful.
	 */
	static void migrate(Vertx vertx, Handler<AsyncResult<Void>> then) {
		migrate(vertx)
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
								LOGGER.info("add MigrationRequest 1: Collections created added if possible");
							}


							try {
								mongo().createIndex(Accounts.USERS, json(UserRecord.EMAILS_ADDRESS, 1));
								mongo().createIndex(INVENTORY, json("userId", 1));
								mongo().createIndex(INVENTORY, json("collectionIds", 1));
								mongo().createIndex(INVENTORY, json("cardDesc.id", 1));
							} finally {
								LOGGER.info("add MigrationRequest 1: Indices added if possible");
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
							mongo().updateCollectionWithOptions(COLLECTIONS, json("heroClass", json("$eq", null)),
									json("$unset", json("deckType", 1), "$set", json("trashed", false)), new UpdateOptions().setMulti(true));

							for (JsonObject record : mongo().findWithOptions(Accounts.USERS, json(), new FindOptions().setFields(json("_id", 1)))) {
								final String userId = record.getString("_id");
								mongo().updateCollectionWithOptions(INVENTORY, json("userId", userId), json("$addToSet", json("collectionIds", userId)), new UpdateOptions().setMulti(true));
							}

							// Remove all inventory records that are in just one collection, the user collection
							mongo().removeDocuments(INVENTORY, json("collectionIds", json("$size", 1)));
						}))
				.add(new MigrationRequest()
						.withVersion(5)
						.withUp(thisVertx -> {
							// Set all existing decks to standard.
							mongo().updateCollectionWithOptions(COLLECTIONS,
									json("format", json("$exists", false)),
									json("$set", json("format", "Standard")),
									new UpdateOptions().setMulti(true));
						})
						.withDown(thisVertx -> {
							// Remove format field
							mongo().updateCollectionWithOptions(COLLECTIONS,
									json("format", json("$exists", true)),
									json("$unset", json("format", null)),
									new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(6)
						.withUp(thisVertx -> {
							// Shuffle around the location of user record data
							List<JsonObject> users = mongo().find(Accounts.USERS, json());
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
								LOGGER.debug("add MigrationRequest 6: Migrating passwords and emails for userId {}", userId);

								mongo().updateCollection(Accounts.USERS, json("_id", userId),
										updateCommand);
							}
						}))
				.add(new MigrationRequest()
						.withVersion(7)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							MongoClientUpdateResult result1 = changeCardId("spell_temporary_anomaly", "spell_temporal_anomaly");
							MongoClientUpdateResult result2 = changeCardId("minion_doomlord", "minion_dreadlord");
							LOGGER.info("add MigrationRequest 7: Fixed {} Temporal Anomaly cards, {} Dreadlord cards", result1.getDocModified(), result2.getDocModified());
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
							LOGGER.info("add MigrationRequest 13: Removed {} bot accounts", botsRemoved);
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
							mongo().updateCollectionWithOptions(COLLECTIONS, json(), json("$set", json(
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
							MongoClientDeleteResult removed = mongo().removeDocuments(INVENTORY, json("cardDesc.id",
									json("$nin", allCardIds)));
							LOGGER.info("add MigrationRequest 23: Removed {} cards that no longer exist", removed.getRemovedCount());
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
							// No more administrative behaviour
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
						}))
				.add(new MigrationRequest()
						.withVersion(30)
						.withUp(thisVertx -> {
							// Reran elsewhere
						}))
				.add(new MigrationRequest()
						.withVersion(31)
						.withUp(thisVertx -> {
							// Reran elsewhere
						}))
				.add(new MigrationRequest()
						.withVersion(32)
						.withUp(thisVertx -> {
							changeCardId("minon_treeleach", "minion_treeleach");
							changeCardId("hero_witch_doctor", "hero_senzaku");
							changeCardId("minion_emerald_exhibit", "minion_ceremonial_alter");
							changeCardId("minion_bladesworn", "minion_entranced_dancer");
							changeCardId("minion_blood_transfuser", "minion_forgotten_ancestor");
							changeCardId("minion_doby_mick", "minion_gaitha_the_protector");
							changeCardId("minion_gurubashi_bloodletter", "minion_hotheaded_villager");
							changeCardId("minion_tunnel_soulfinder", "minion_jungle_soulfinder");
							changeCardId("minion_anzu_the_raven_god", "minion_kinru_the_benevolent");
							changeCardId("minion_spore_bat", "minion_prized_boar");
							changeCardId("minion_winterfang_seer", "minion_rallying_elder");
							changeCardId("minion_darkwill_appraiser", "minion_resourceful_hoarder");
							changeCardId("minion_wolpertinger", "minion_shimmerscale");
							changeCardId("minion_ghuun_the_false_god", "minion_soulcaller_roten");
							changeCardId("minion_zandalari_conjurer", "minion_spiritcaller");
							changeCardId("minion_avatar_of_hakkar", "minion_survivalist_yukono");
							changeCardId("minion_aberration", "minion_temple_militia");
							changeCardId("minion_nraqi_oppressor", "minion_underbrush_protector");
							changeCardId("minion_energized_spectre", "minion_undergrowth_spirit");
							changeCardId("minion_bwonsamdi_witchdoctor", "minion_ushibasu_the_vigilant");
							changeCardId("spell_shadow_distortion", "spell_alter_ego");
							changeCardId("spell_cycle_of_undeath", "spell_ashes_to_ashes_voodoo");
							changeCardId("spell_mass_production", "spell_beastcallers_summon");
							changeCardId("spell_cursed_mirror", "spell_bodyswap");
							changeCardId("spell_blood_rage", "spell_hex_behemoth");
							changeCardId("spell_hex_zombie", "spell_hex_pig");
							changeCardId("spell_spiritball", "spell_jungles_guidance");
							changeCardId("spell_kanov_worms", "spell_law_of_the_jungle");
							changeCardId("spell_drain_sanity", "spell_manic_outburst");
							changeCardId("spell_bone_bond", "spell_wicked_insight");
							changeCardId("token_spiritfused_machine", "token_hungry_jaguar");
							changeCardId("token_voodoo_pig", "token_sacrificial_pig");
							changeCardId("weapon_staff_of_origination", "weapon_hexcarver");
							changeCardId("weapon_butchers_cleaver", "weapon_sacrificial_blade");
							changeCardId("weapon_spirit_wand", "weapon_twistbark_staff");
							changeCardId("minion_student_of_the_tiger", "minion_lungrath_hunter");
							changeCardId("hero_chen_stormstout", "hero_mienzhou");
							changeCardId("hero_power_meditation", "hero_power_effuse");
							changeCardId("minion_blessed_koi_statue", "minion_jade_serpent_statue");
							changeCardId("minion_skunky_brew_alemental", "minion_deepwoods_elemental");
							changeCardId("minion_crane_school_instructor", "minion_desciple_of_shitakiri");
							changeCardId("minion_black_ox_statue", "minion_enchanted_tapestry");
							changeCardId("minion_emperor_shaohao", "minion_master_jigen");
							changeCardId("minion_monastery_guard", "minion_monastery_warden");
							changeCardId("minion_windwalk_master", "minion_shigaraki_elder");
							changeCardId("minion_elusive_brawler", "minion_sly_brawler");
							changeCardId("spell_leg_sweep", "spell_axe_kick");
							changeCardId("spell_chi_torpedo", "spell_chi_restoration");
							changeCardId("spell_gift_of_the_mists", "spell_enlightenment");
							changeCardId("spell_flying_serpent_kick", "spell_fiery_kitsune_punch");
							changeCardId("spell_fortifying_brew", "spell_fortifying_prayer");
							changeCardId("spell_storm_earth_and_fire", "spell_fury_of_the_elements");
							changeCardId("spell_staggering_brew", "spell_honed_potion");
							changeCardId("spell_keg_smash", "spell_mark_of_despair");
							changeCardId("spell_tiger_palm_strike", "spell_palm_strike");
							changeCardId("spell_effuse", "spell_springs_of_ebisu");
							changeCardId("spell_dampen_harm", "spell_steadfast_defense");
							changeCardId("spell_breath_of_fire", "spell_windswept_strike");
							changeCardId("token_xuen_the_white_tiger", "token_kumiho_nine_tailed_kitsune");
							changeCardId("token_chi_ji_the_red_crane", "token_shitakiri_slit_tongue_suzume");
							changeCardId("token_tiny_alemental", "token_stony_elemental");
							changeCardId("token_earth_spirit", "token_unearthed_spirit");
							changeCardId("token_niuzao_the_black_ox", "token_yashima_cheerful_tanuki");
							// Rerun the earlier changes since something definitely glitched out
							changeCardId("minion_anub'rekhan", "minion_anobii");
							changeCardId("minion_azjol_visionary", "minion_visionary");
							changeCardId("minion_nerubian_vizier", "minion_vizier");
							changeCardId("weapon_maexxnas_femur", "weapon_scepter_of_bees");
							changeCardId("minion_qiraji_guardian", "minion_grand_guardian");
							changeCardId("minion_prophet_skeram", "minion_vermancer_prophet");
							changeCardId("minion_silithid_wasp", "minion_servant_wasp");
							changeCardId("spell_elementium_shell", "spell_reinforced_shell");
							changeCardId("spell_ahnqiraj_portal", "spell_ancient_waygate");
						}))
				.add(new MigrationRequest()
						.withVersion(33)
						.withUp(thisVertx -> {
							changeCardId("minion_jade_serpent_statue", "minion_jade_cloud_serpent");
							changeCardId("token_storm_spirit", "token_bellowing_spirit");
							changeCardId("token_fire_spirit", "token_burning_spirit");
						}))
				.add(new MigrationRequest()
						.withVersion(34)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(35)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(36)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							// We'll mark as removed all the missing cards, then change the hero classes that no longer exist to
							// neutral heroes.

							// First, change all missing cards to reference the "removed card"
							List<String> cardIdsInCatalogue = new ArrayList<>(CardCatalogue.getRecords().keySet());
							String removedCardId = BaseCardResources.REMOVED_CARD_ID;
							mongo().updateCollectionWithOptions(
									INVENTORY,
									json(InventoryRecord.CARDDESC_ID, json("$nin", cardIdsInCatalogue)),
									json("$set", json(InventoryRecord.CARDDESC_ID, removedCardId)),
									new UpdateOptions().setMulti(true));

							// Change all the hero classes to the neutral class.
							List<String> heroClasses = HeroClass.getBaseClasses(DeckFormat.spellsource());
							mongo().updateCollectionWithOptions(
									COLLECTIONS,
									json(CollectionRecord.HERO_CLASS, json("$nin", heroClasses)),
									json("$set", json(CollectionRecord.HERO_CLASS, HeroClass.ANY)),
									new UpdateOptions().setMulti(true));

							List<String> formats = new ArrayList<>(DeckFormat.formats().keySet());
							// Remove all missing formats
							mongo().updateCollectionWithOptions(
									COLLECTIONS,
									json(CollectionRecord.FORMAT, json("$nin", formats)),
									json("$set", json(CollectionRecord.FORMAT, DeckFormat.ALL.getName())),
									new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(37)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							changeCardId("minion_lifetaker", "minion_marrow_render");
							changeCardId("spell_blood_plague_blood_knight", "spell_blood_plague_vampire");
							changeCardId("weapon_midnight_blade", "weapon_wicked_moonblade");
							changeCardId("spell_crimson_flow", "spell_hemoshape_flow");
							changeCardId("minion_thassarian", "minion_the_vein_wyrm");
							changeCardId("weapon_souldrinker_axe", "weapon_consuming_blade");
						}))
				.add(new MigrationRequest()
						.withVersion(38)
						.withUp(thisVertx -> {
							mongo().updateCollectionWithOptions(Accounts.USERS,
									json(),
									json("$unset", json("roles", null)),
									new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(39)
						.withUp(thisVertx -> {
							// Remove all the presence status from mongo, it will be computed on the fly
							MongoClientUpdateResult res;
							do {
								res = mongo().updateCollectionWithOptions(Accounts.USERS,
										json("friends.presence", json("$exists", true)),
										json("$unset", json("friends.$.presence", null)), new UpdateOptions().setMulti(true));
							} while (res.getDocModified() != 0);
						}))
				.add(new MigrationRequest()
						.withVersion(40)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							removeCards(
									"totemic_split",
									"secret_ichor_conversion",
									"minion_felstalker",
									"minion_shadow_satyr",
									"spell_nightmare_portal",
									"hero_nemsy_awakened_calamity",
									"minion_faceless_nightmare");
						}))
				.add(new MigrationRequest()
						.withVersion(41)
						.withUp(thisVertx -> {
							// Fix all the busted decks players have made
							Decks.validateAllDecks();
						}))
				.add(new MigrationRequest()
						.withVersion(42)
						.withUp(thisVertx -> {
							// Remove unused formats
							CardCatalogue.loadCardsFromPackage();
							mongo().updateCollectionWithOptions(COLLECTIONS, json(CollectionRecord.FORMAT,
									json("$nin", array("Spellsource", "All", "Pauper"))),
									json("$set", json(CollectionRecord.FORMAT, "All")), new UpdateOptions().setMulti(true));
						}))
				.add(new MigrationRequest()
						.withVersion(43)
						.withUp(thisVertx -> {
							mongo().createCollection(Editor.EDITABLE_CARDS);
						}))
				.add(new MigrationRequest()
						.withVersion(44)
						.withUp(thisVertx -> {
							CardCatalogue.loadCardsFromPackage();
							Bots.updateBotDeckList();
						}))
				.add(new MigrationRequest()
						.withVersion(45)
						.withUp(thisVertx -> {
							removeCards("spell_earthquake");
						}))
				.migrateTo(45, then2 ->
						then.handle(then2.succeeded() ? Future.succeededFuture() : Future.failedFuture(then2.cause())))
		;
	}

	@Suspendable
	static MongoClientUpdateResult changeCardId(String oldId, String newId) {
		CardCatalogue.loadCardsFromPackage();
		try {
			CardCatalogue.getCardById(newId);
		} catch (Throwable any) {
			LOGGER.error("changeCardId: Cannot change {} to {} because the new ID does not exist", oldId, newId);
			return new MongoClientUpdateResult();
		}

		return Mongo.mongo().updateCollectionWithOptions(INVENTORY,
				json("cardDesc.id", oldId), json("$set", json("cardDesc.id", newId)), new UpdateOptions().setMulti(true));
	}

	@Suspendable
	static MongoClientDeleteResult removeCards(String... ids) {
		return Mongo.mongo().removeDocuments(INVENTORY, json("cardDesc.id",
				json("$in", array(ids))));
	}

	/**
	 * Adds a given up and down function to a specific version.
	 *
	 * @param request A specification for a migration.
	 * @return An empty object indicating the migration was successfully registered.
	 */
	MigrationResponse add(MigrationRequest request);

	/**
	 * A migration request.
	 *
	 * @param request A version to migrate up or down to, or a request to migrate to the latest version.
	 * @return {@link MigrationToResponse#succeededMigration()} if the migration succeeded, otherwise {@link
	 * MigrationToResponse#failedMigration()} if it failed.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	MigrationToResponse migrateTo(MigrateToRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Force the database to be unlocked. Very dangerous.
	 *
	 * @param ignoredRequest Ignored parameter.
	 * @return A void parameter.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	Serializable forceUnlock(Serializable ignoredRequest) throws InterruptedException, SuspendExecution;

	/**
	 * Gets a {@link Migrator} that can be used to easily run a migration
	 *
	 * @param vertx
	 * @return
	 */
	static Migrator migrate(Vertx vertx) {
		return new MigratorImpl(vertx);
	}
}
