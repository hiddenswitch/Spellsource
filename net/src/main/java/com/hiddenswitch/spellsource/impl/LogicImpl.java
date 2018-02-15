package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Decks;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.Spellsource;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.Inventory;
import com.hiddenswitch.spellsource.Logic;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.util.RpcClient;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckWithId;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.AttributeMap;

import java.util.*;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

public class LogicImpl extends AbstractService<LogicImpl> implements Logic {
	private RpcClient<Inventory> inventory;
	private RpcClient<Decks> decks;
	private Registration registration;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		inventory = Rpc.connect(Inventory.class, vertx.eventBus());
		decks = Rpc.connect(Decks.class, vertx.eventBus());
		registration = Rpc.register(this, Logic.class, vertx.eventBus());

		// Register new persistence effects
		Spellsource.spellsource().persistAttribute(LegacyPersistenceHandler.create(
				"unique-champion-ids-1",
				GameEventType.BEFORE_SUMMON,
				this::beforeSummon,
				PersistenceTrigger::beforeSummon));

		/*
		Spellsource.spellsource().persistAttribute(
				"unique-champion-ids-1",
				GameEventType.AFTER_SUMMON,
				Attribute.UNIQUE_CHAMPION_IDS,
				(PersistenceContext<AfterSummonEvent> context) -> {
					if (context.event().getMinion() == null) {
						return;
					}

					if (context.event().getMinion().getSourceCard() == null) {
						return;
					}

					// Only count summons from the hand
					if (context.event().getSource() == null) {
						return;
					}

					String inventoryIds

					int attackerDamage = context.event().getDamageDealt();
					context.update(context.event().getAttacker().getReference(),
							context.event().getAttacker().getAttributeValue(Attribute.TOTAL_DAMAGE_DEALT) + attackerDamage);
				});
				*/

		Spellsource.spellsource().persistAttribute(LegacyPersistenceHandler.create(
				"last-minion-destroyed-1",
				GameEventType.AFTER_PHYSICAL_ATTACK,
				this::afterPhysicalAttack,
				PersistenceTrigger::afterPhysicalAttack));

		Spellsource.spellsource().persistAttribute(
				"total-damage-dealt-1",
				GameEventType.AFTER_PHYSICAL_ATTACK,
				Attribute.TOTAL_DAMAGE_DEALT,
				(PersistenceContext<AfterPhysicalAttackEvent> context) -> {
					int attackerDamage = context.event().getDamageDealt();
					context.update(context.event().getAttacker().getReference(),
							context.event().getAttacker().getAttributeValue(Attribute.TOTAL_DAMAGE_DEALT) + attackerDamage);
				}
		);

		Spellsource.spellsource().persistAttribute(
				"one-upper-1",
				GameEventType.KILL,
				Attribute.WEAKEST_ON_BATTLEFIELD_WHEN_DESTROYED_COUNT,
				(PersistenceContext<KillEvent> context) -> {
					Actor victim = (Actor) context.event().getVictim();

					final GameContext gameContext = context.event().getGameContext();
					Optional<Minion> lowestAttackMinionStillOnBattlefield = gameContext.getEntities()
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

	@Override
	@Suspendable
	public InitializeUserResponse initializeUser(InitializeUserRequest request) throws SuspendExecution,
			InterruptedException {
		final InitializeUserResponse response = new InitializeUserResponse();
		response.setDeckCreateResponses(new ArrayList<>());
		final String userId = request.getUserId();
		if (userId == null) {
			throw new RuntimeException();
		}

		response.setCreateCollectionResponse(inventory.sync().createCollection(CreateCollectionRequest.startingCollection(userId)));

		// Load in the starting deck lists
		List<DeckCreateRequest> standardDecks = Spellsource.spellsource().getStandardDecks();
		for (DeckCreateRequest deckCreateRequest : standardDecks) {
			final DeckCreateResponse deckCreate = decks.sync().createDeck(deckCreateRequest.clone().withUserId(userId));
			response.getDeckCreateResponses().add(deckCreate);
			response.getCreateCollectionResponse().getCreatedInventoryIds().addAll(deckCreate.getInventoryIds());
		}

		return response;
	}

	@Override
	public EndGameResponse endGame(EndGameRequest request) throws SuspendExecution, InterruptedException {
		// Return all currently borrowed decks
		final List<String> deckIds = request.getPlayers().stream().map(EndGameRequest.Player::getDeckId).collect
				(Collectors.toList());
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
					.withUserId(player.getUserId()).withDeckId(player.getDeckId()));

			String name = Accounts.findOne(player.getUserId()).getUsername();

			// TODO: Get more attributes from database
			AttributeMap playerAttributes = new AttributeMap();
			playerAttributes.put(Attribute.NAME, name);
			playerAttributes.put(Attribute.USER_ID, player.getUserId());
			playerAttributes.put(Attribute.DECK_ID, player.getDeckId());

			// Create the deck and assign all the appropriate IDs to the cards
			Deck deck = new DeckWithId(player.getDeckId());
			deck.setHeroClass(deckCollection.getHeroClass());
			deck.setName(deckCollection.getName());
			String heroCardId = deckCollection.getHeroCardId();
			if (heroCardId != null) {
				deck.setHeroCard((HeroCard) CardCatalogue.getCardById(heroCardId));
			}

			deckCollection.getInventoryRecords().stream().map(cardRecord -> Logic.getDescriptionFromRecord(cardRecord,
					player.getUserId(), player.getDeckId())).map(CardDesc::createInstance).forEach(deck.getCards()
					::addCard);

			// TODO: Add player information as attached to the hero entity
			response.getPlayers().set(player.getId(), new StartGameResponse.Player().withDeck(deck).withAttributes
					(playerAttributes));

		}

		// Borrow the decks
		final List<String> deckIds = request.getPlayers().stream().map(StartGameRequest.Player::getDeckId).collect
				(Collectors.toList());
		inventory.sync().borrowFromCollection(new BorrowFromCollectionRequest().withCollectionIds(deckIds));

		return response;
	}

	/**
	 * Handles the networked effects when a minion is summoned.
	 * <p>
	 * For example, The Forever Post-Doc is a minion whose text reads:
	 * <p>
	 * <code>Call to Arms: If this is the first time you've played this minion, permanently cost (1) less.</code>
	 * <p>
	 * Every time Forever Post-Doc is summoned, the Games service knows it must call beforeSummon to process the
	 * minion's persistent side effects. It will return the correct change in its cost for the Games service to apply to
	 * the live running game.
	 *
	 * @param request Information about the summoned minion.
	 * @return The side effects of summoning the minion which affect the game.
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	@Suspendable
	public LogicResponse beforeSummon(EventLogicRequest<BeforeSummonEvent> request) {
		LogicResponse response = new LogicResponse();
		final String userId = request.getUserId();
		final String gameId = request.getGameId();
		final BeforeSummonEvent beforeSummonEvent = request.getEvent();

		if (beforeSummonEvent == null || beforeSummonEvent.getEventType() != GameEventType.BEFORE_SUMMON
				|| beforeSummonEvent.getMinion() == null) {
			throw new RuntimeException();
		}

		final String id = beforeSummonEvent.getMinion().getCardInventoryId();
		final int entityId = beforeSummonEvent.getMinion().getId();

		if (id == null) {
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

		if (!updated) {
			return LogicResponse.empty();
		}

		response.withGameIdsAffected(Collections.singletonList(gameId))
				.withEntityIdsAffected(Collections.singletonList(entityId));
		AttributeMap map = new AttributeMap();
		map.put(Attribute.UNIQUE_CHAMPION_IDS_SIZE, beforeSummonEvent.getMinion().getAttributeValue(Attribute
				.UNIQUE_CHAMPION_IDS_SIZE) + 1);
		response.getModifiedAttributes().put(new EntityReference(entityId), map);


		return response;
	}

	/**
	 * Handles the networked effects when an actor attacks another actor.
	 * <p>
	 * For example, Sourcing Specialist is a minion whose text reads:
	 * <p>
	 * <code>Call to Arms: Summon the last minion Sourcing Specialist destroyed.</code>
	 * <p>
	 * Whenever Sourcing Specialist attacks and destroys its target, this method will correctly record the last minion
	 * it destroyed. Other code inside Sourcing Specialist looks up the attribute "LAST_MINION_DESTROYED_ID" to summon
	 * the actual minion. The purpose of this method is to record the last minion destroyed, but not to actually perform
	 * in-game summoning.
	 *
	 * @param request Information about the physical attack.
	 * @return The side effects of the physical attack which affect the game.
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	@Suspendable
	public LogicResponse afterPhysicalAttack(EventLogicRequest<AfterPhysicalAttackEvent> request) {
		LogicResponse response = new LogicResponse();
		final String gameId = request.getGameId();
		final AfterPhysicalAttackEvent event = request.getEvent();

		if (event == null || event.getEventType() != GameEventType.AFTER_PHYSICAL_ATTACK) {
			throw new RuntimeException();
		}

		final int entityId = event.getAttacker().getId();

		// If the defender got destroyed, we need to update the last minion destroyed for the attacker
		if (!event.getDefender().isDestroyed()) {
			return LogicResponse.empty();
		}

		final String attackerInventoryId = event.getAttacker().getCardInventoryId();
		final String defenderCardInventoryId = event.getDefender().getCardInventoryId();
		final String defenderCardId = event.getDefender().getSourceCard().getCardId();

		MongoClientUpdateResult update = awaitResult(h -> getMongo()
				.updateCollection(Inventory.INVENTORY,
						json("_id", attackerInventoryId),
						json("$set", json("facts.lastMinionDestroyedCardId", defenderCardId,
								"facts.lastMinionDestroyedInventoryId", defenderCardInventoryId)), h));

		response.withGameIdsAffected(Collections.singletonList(gameId))
				.withEntityIdsAffected(Collections.singletonList(entityId));

		AttributeMap map = new AttributeMap();
		map.put(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID, defenderCardInventoryId);
		map.put(Attribute.LAST_MINION_DESTROYED_CARD_ID, defenderCardId);
		response.getModifiedAttributes().put(new EntityReference(entityId), map);

		return response;
	}

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	public PersistAttributeResponse persistAttribute(PersistAttributeRequest request) {
		if (request.getRequest() != null) {
			return new PersistAttributeResponse().withResponse(Spellsource.spellsource().persistence().getLogicHandler(request
					.getId()).onLogicRequest(request.getRequest()));
		} else {
			final String attributeName = request.getAttribute().toKeyCase();
			MongoClientUpdateResult update = Inventory.update(getMongo(), request.getInventoryIds(), json(
					"$set", json("facts." + attributeName, request.getNewValue()))
			);
			return new PersistAttributeResponse().withUpdated(update.getDocModified());
		}
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
