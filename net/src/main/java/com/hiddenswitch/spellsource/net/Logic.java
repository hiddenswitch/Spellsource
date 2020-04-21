package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.net.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.net.impl.util.PersistenceContext;
import com.hiddenswitch.spellsource.net.impl.util.PersistenceTrigger;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.impl.Mongo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.events.KillEvent;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;

/**
 * A Logic service that handles complex game logic.
 * <p>
 * To implement a new persistence effect, see {@link Spellsource#persistAttribute(String, EventTypeEnum, Attribute,
 * SuspendableAction1)}.
 */
public interface Logic {
	Logger logger = LoggerFactory.getLogger(Logic.class);

	/**
	 * Initializes custom networked attributes in network-only games (i.e. {@link com.hiddenswitch.spellsource.net.impl.util.ServerGameContext})
	 * like Legacy effects.
	 */
	static void triggers() {
		Spellsource.spellsource().persistAttribute(
				"last-minion-destroyed-1",
				com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK,
				Attribute.LAST_MINION_DESTROYED_INVENTORY_ID,
				(PersistenceContext<AfterPhysicalAttackEvent> context) -> {
					if (context.event().getDefender() == null
							|| context.event().getAttacker() == null
							|| context.event().getDefender().getCardInventoryId() == null) {
						return;
					}
					context.update(context.event().getAttacker().getReference(), context.event().getDefender().getCardInventoryId());
				});

		Spellsource.spellsource().persistAttribute(
				"last-minion-destroyed-2",
				com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK,
				Attribute.LAST_MINION_DESTROYED_CARD_ID,
				(PersistenceContext<AfterPhysicalAttackEvent> context) -> {
					if (context.event().getDefender() == null
							|| context.event().getAttacker() == null
							|| context.event().getDefender().getSourceCard() == null) {
						return;
					}
					context.update(context.event().getAttacker().getReference(), context.event().getDefender().getSourceCard().getCardId());
				});

		Spellsource.spellsource().persistAttribute(
				"total-damage-dealt-1",
				com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_PHYSICAL_ATTACK,
				Attribute.TOTAL_DAMAGE_DEALT,
				(PersistenceContext<AfterPhysicalAttackEvent> context) -> {
					int attackerDamage = context.event().getDamageDealt();
					context.update(context.event().getAttacker().getReference(),
							context.event().getAttacker().getAttributeValue(Attribute.TOTAL_DAMAGE_DEALT) + attackerDamage);
				}
		);

		Spellsource.spellsource().persistAttribute(
				"unique-champion-ids-1",
				com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.BEFORE_SUMMON,
				Attribute.UNIQUE_CHAMPION_IDS_SIZE,
				(PersistenceContext<BeforeSummonEvent> context) -> {
					if (context.event().getSource().getCardInventoryId() == null) {
						return;
					}

					context.update(context.event().getSource().getReference(),
							context.event().getSource().getAttributeValue(Attribute.UNIQUE_CHAMPION_IDS_SIZE) + 1);
				}
		);

		Spellsource.spellsource().persistAttribute(
				"one-upper-1",
				com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.KILL,
				Attribute.WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT,
				(PersistenceContext<KillEvent> context) -> {
					Actor victim = (Actor) context.event().getTarget();

					GameContext gameContext = context.event().getGameContext();
					Optional<Minion> lowestAttackMinionStillOnBattlefield = gameContext.getEntities()
							.filter(Objects::nonNull)
							.filter(e -> e.getEntityType() == EntityType.MINION)
							.filter(e -> e.getZone() == Zones.BATTLEFIELD)
							.map(e -> (Minion) e)
							.min(Comparator.comparingInt(Minion::getAttack));

					if (lowestAttackMinionStillOnBattlefield.isPresent()
							&& victim.getAttack() < lowestAttackMinionStillOnBattlefield.get().getAttack()) {
						context.update(victim.getReference(), victim.getAttributeValue(Attribute.WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT) + 1);
					}
				}
		);
	}

	/**
	 * Performs account creation action side effects, like adding the first cards to the player's collection, defining
	 * their starting decks and in the future, creating friend recommendations.
	 * <p>
	 * Some users, like test users or some kinds of bots, will not need starting decks or starting inventory.
	 *
	 * @param request The user to "initialize" for.
	 * @return Information about what this method did, like which decks it created and which cards the user was awarded.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	static InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution, InterruptedException {
		final InitializeUserResponse response = new InitializeUserResponse();
		response.setDeckCreateResponses(new ArrayList<>());
		final String userId = request.getUserId();
		if (userId == null) {
			throw new RuntimeException();
		}

		response.setCreateCollectionResponse(Inventory.createCollection(CreateCollectionRequest.startingCollection(userId)));

		// Load in the starting deck lists
		List<DeckCreateRequest> standardDecks = Spellsource.spellsource().getStandardDecks();
		for (DeckCreateRequest deckCreateRequest : standardDecks) {
			final DeckCreateResponse deckCreate = Decks.createDeck(deckCreateRequest.clone().withUserId(userId));
			response.getDeckCreateResponses().add(deckCreate);
			response.getCreateCollectionResponse().getCreatedInventoryIds().addAll(deckCreate.getInventoryIds());
		}

		return response;
	}

	/**
	 * Gracefully ends a game. This will return decks that are currently in use.
	 *
	 * @param request The game to end
	 * @return Information about the game that was ended.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static EndGameResponse endGame(EndGameRequest request) throws SuspendExecution, InterruptedException {
		// Return all currently borrowed decks
		final List<String> deckIds = request.getPlayers().stream().map(EndGameRequest.Player::getDeckId).collect
				(Collectors.toList());
		Inventory.returnToCollection(new ReturnToCollectionRequest().withDeckIds(deckIds));

		return new EndGameResponse();
	}

	/**
	 * Converts an inventory record into a {@link CardDesc}, that eventually gets turned into an {@link
	 * net.demilich.metastone.game.cards.Card} in the game.
	 *
	 * @param cardRecord The record from the database describing a card in a player's collection.
	 * @param userId     The player to whom this card belongs.
	 * @param deckId     The deck that the caller is requesting this description record for.
	 * @return A completed card description.
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	static CardDesc getDescriptionFromRecord(InventoryRecord cardRecord, String userId, String deckId) {
		try {
			// Set up the attributes
			String cardId = cardRecord.getCardDesc().getId();
			final Card cardById = CardCatalogue.getCardById(cardId);
			if (cardById == null) {
				logger.error("getDescriptionFromRecord: Card with desc.id={} was not found", cardId);
				return null;
			}

			CardDesc desc = cardById.getDesc().clone();

			if (desc.getAttributes() == null) {
				desc.setAttributes(new AttributeMap());
			}

			desc.getAttributes().put(Attribute.USER_ID, userId);
			desc.getAttributes().put(Attribute.CARD_INVENTORY_ID, cardRecord.getId());
			desc.getAttributes().put(Attribute.DECK_ID, deckId);
			desc.getAttributes().put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
			desc.getAttributes().put(Attribute.CHAMPION_ID, userId);
			desc.getAttributes().put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
			desc.getAttributes().put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
			desc.getAttributes().put(Attribute.ENTITY_INSTANCE_ID, RandomStringUtils.randomAlphanumeric(20).toLowerCase());

			// Collect the persistent attributes
			desc.getAttributes().putAll(cardRecord.getPersistentAttributes());

			return desc;
		} catch (Exception ex) {
			logger.error("getDescriptionFromRecord: Error {} retrieving data for userId={}, deckId={}, cardRecord={}", ex, userId, deckId, cardRecord);
			return null;
		}
	}

	/**
	 * Persists the requested attribute for an inventory ID.
	 *
	 * @param request The information abotu the attribute, inventory item and game necessary to save it to the database.
	 * @return The results to apply to the entity.
	 */
	@Suspendable
	static PersistAttributeResponse persistAttribute(PersistAttributeRequest request) {
		final String attributeName = request.getAttribute().toKeyCase();
		MongoClientUpdateResult update = Mongo.mongo().updateCollectionWithOptions(Inventory.INVENTORY, json("_id", json("$in", request.getInventoryIds())), json(
				"$set", json("facts." + attributeName, request.getNewValue())), new UpdateOptions().setMulti(true));
		return new PersistAttributeResponse().withUpdated(update.getDocModified());
	}

	/**
	 * Retrieves a deck of a specific name from the player's collection.
	 * <p>
	 * The search is case insensitive
	 *
	 * @param logicGetDeckRequest The request specifying the name and user ID of the player whose collection should be
	 *                            queried.
	 * @return A response containing the deck if it exists.
	 */
	@Suspendable
	static GetCollectionResponse getDeck(LogicGetDeckRequest logicGetDeckRequest) {
		// Looks up a deck by name
		List<JsonObject> collections = Mongo.mongo().findWithOptions(Inventory.COLLECTIONS,
				json("userId", logicGetDeckRequest.getUserId().toString(), "name", json("$regex", "^" + logicGetDeckRequest.getName() + "$", "$options", "i")),
				new FindOptions().setFields(json("_id", 1)));

		if (collections.size() == 0) {
			return GetCollectionResponse.empty();
		}

		return Inventory.getCollection(GetCollectionRequest.deck(collections.get(0).getString("_id")));
	}
}
