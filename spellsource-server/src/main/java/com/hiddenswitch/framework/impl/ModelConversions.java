package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Message;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.framework.schema.spellsource.tables.interfaces.ICardsInDeck;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.CardsInDeck;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.events.JoustEvent;
import net.demilich.metastone.game.events.SecretRevealedEvent;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Aftermath;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.WhereverTheyAreEnchantment;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelConversions {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelConversions.class);
	private static ObjectMapper caseInsensitiveMapper = new ObjectMapper().registerModule(new ProtobufModule())
			.setAnnotationIntrospector(new JacksonAnnotationIntrospector())
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	/**
	 * Get an entity representing a censored secret card.
	 *
	 * @param id        The card's entity ID
	 * @param owner     The secret's owner
	 * @param location  The secret'slocation
	 * @param heroClass The hero class of the secret
	 * @return A censored secret card.
	 */
	static Entity.Builder getCensoredCard(int id, int owner, net.demilich.metastone.game.entities.EntityLocation location, String heroClass) {
		return Entity.newBuilder()
				.setCardId("hidden")
				.setEntityType(EntityType.CARD)
				.setDescription("A secret! This card will be revealed when a certain action occurs.")
				.setName("Secret")
				.setId(id)
				.setOwner(owner)
				.setCardType(CardType.SPELL)
				.setHeroClass(heroClass)
				.setLocation(toClientLocation(location));
	}

	/**
	 * Converts an inventory record into a {@link CardDesc}, that eventually gets turned into an
	 * {@link net.demilich.metastone.game.cards.Card} in the game.
	 *
	 * @param cardRecord The record from the database describing a card in a player's collection.
	 * @param userId     The player to whom this card belongs.
	 * @param deckId     The deck that the caller is requesting this description record for.
	 * @return A completed card description.
	 */
	public static CardDesc getDescriptionFromRecord(ICardsInDeck cardRecord, String userId, String deckId) {
		// Set up the attributes
		var cardId = cardRecord.getCardId();
		if (!ClasspathCardCatalogue.INSTANCE.getCards().containsKey(cardId.toLowerCase())) {
			throw new NullPointerException(String.format("card not found cardId=%s", cardRecord.getCardId()));
		}

		var cardById = ClasspathCardCatalogue.INSTANCE.getCardById(cardId);
		var desc = cardById.getDesc().clone();

		if (desc.getAttributes() == null) {
			desc.setAttributes(new AttributeMap());
		}

		desc.getAttributes().put(Attribute.USER_ID, userId);
		desc.getAttributes().put(Attribute.CARD_INVENTORY_ID, cardRecord.getId());
		desc.getAttributes().put(Attribute.DECK_ID, deckId);
//			desc.getAttributes().put(Attribute.DONOR_ID, cardRecord.getDonorUserId());
		desc.getAttributes().put(Attribute.CHAMPION_ID, userId);
//			desc.getAttributes().put(Attribute.COLLECTION_IDS, cardRecord.getCollectionIds());
//			desc.getAttributes().put(Attribute.ALLIANCE_ID, cardRecord.getAllianceId());
		desc.getAttributes().put(Attribute.ENTITY_INSTANCE_ID, UUID.randomUUID().toString());

		// Collect the persistent attributes
//			desc.getAttributes().putAll(cardRecord.getPersistentAttributes());

		return desc;
	}

	/**
	 * Get a client's view of the current game actions.
	 *
	 * @param workingContext A game context that contains the players and state.
	 * @param actions        The possible actions ot process against the context.
	 * @param playerId       The player to process actions for.
	 * @return A list of game client actions.
	 */
	static GameActions getClientActions(GameContext workingContext, List<GameAction> actions, int playerId) {
		class ActionKey {
			private int sourceReference;
			private ActionType actionType;

			private ActionKey(GameAction gameAction) {
				this.sourceReference = gameAction.getSourceReference() == null ? -1 : gameAction.getSourceReference().getId();
				this.actionType = gameAction.getActionType();
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (!(o instanceof ActionKey)) return false;
				var actionKey = (ActionKey) o;
				return sourceReference == actionKey.sourceReference &&
						actionType == actionKey.actionType;
			}

			@Override
			public int hashCode() {
				return Objects.hash(sourceReference, actionType);
			}
		}

		var actionMap = actions.stream()
				.unordered()
				.collect(Collectors.groupingBy(ActionKey::new));
		var friendlyMinions = workingContext.getPlayer(playerId).getMinions();
		var discovers = workingContext.getPlayer(playerId).getDiscoverZone();
		var clientActions = GameActions.newBuilder()
				.addAllAll(
						actionMap.entrySet()
								.stream()
								.unordered()
								.flatMap(kv -> {
									if (kv.getKey().actionType == ActionType.SUMMON) {
										return Stream.of(SpellAction.newBuilder()
												.setSourceId(kv.getKey().sourceReference)
												.setActionType(kv.getKey().actionType)
												.addAllTargetKeyToActions(kv.getValue().stream().map(ga -> TargetActionPair.newBuilder()
														.setAction(ga.getId())
														.setFriendlyBattlefieldIndex(friendlyMinions.stream().filter(m -> Objects.equals(m.getReference(), ga.getTargetReference())).map(Minion::getIndex).findFirst().orElse(friendlyMinions.size()))
														.setTarget((ga.getTargetReference() == null || Objects.equals(ga.getTargetReference(), EntityReference.NONE)) ? -1 : ga.getTargetReference().getId()).build()
												).collect(toList())).build());
									} else if (kv.getKey().actionType == ActionType.DISCOVER) {
										// Find the corresponding cards in the discover zone
										kv.getValue().sort(Comparator.comparingInt(GameAction::getId));
										return IntStream.range(0, discovers.size())
												.mapToObj(i -> {
													var sourceAction = kv.getValue().get(i);
													return SpellAction.newBuilder()
															.setSourceId(discovers.get(i).getId())
															.setAction(sourceAction.getId())
															.setActionType(ActionType.DISCOVER).build();
												});
									} else if (kv.getValue().get(0).getTargetRequirement() == TargetSelection.NONE) {
										var ga = kv.getValue().get(0);
										return Stream.of(SpellAction.newBuilder()
												.setSourceId(kv.getKey().sourceReference)
												.setAction(ga.getId())
												.setEntity(ga instanceof HasCard ?
														getEntity(workingContext, ((HasCard) ga).getSourceCard(), playerId) : Entity.newBuilder())
												.setDescription(ga.getDescription(workingContext, playerId))
												.setActionType(ga.getActionType()).build());
									} else {
										return Stream.of(SpellAction.newBuilder()
												.setSourceId(kv.getKey().sourceReference)
												.setActionType(kv.getKey().actionType)
												.addAllTargetKeyToActions(kv.getValue().stream().map(ga -> TargetActionPair.newBuilder()
														.setAction(ga.getId())
														.setTarget((ga.getTargetReference() == null || Objects.equals(ga.getTargetReference(), EntityReference.NONE)) ? -1 : ga.getTargetReference().getId()).build()).collect(toList())).build());
									}
								})
								.collect(toList())
				);

		// Add all the action indices for compatibility purposes
		clientActions.addAllCompatibility(actions.stream()
				.map(GameAction::getId)
				.collect(toList()));

		return clientActions.build();
	}

	/**
	 * Gets a client view of a game event.
	 * <p>
	 * This method does not correctly consider security issues accurately. It leaks which cards the opponent draws and
	 * which secrets the opponent plays. In the future, it will respect these limitations.
	 *
	 * @param event    A game engine event.
	 * @param playerId The player requesting the view.
	 * @return A client-specific view of the event.
	 */
	static GameEvent.Builder getClientEvent(net.demilich.metastone.game.events.GameEvent event, int playerId) {
		var clientEvent = GameEvent.newBuilder();

		clientEvent.setEventType(event.getEventType());

		var workingContext = event.getGameContext().clone();
		var source = event.getSource(workingContext);
		var target = event.getTarget();
		var targets = event.getTargets(workingContext, playerId);
		var value = event instanceof HasValue ? ((HasValue) event).getValue() : null;
		var card = event instanceof HasCard ? ((HasCard) event).getSourceCard() : null;
		var description = event.isPowerHistory() ? event.getDescription(workingContext, playerId) : "";

		clientEvent.setIsPowerHistory(event.isPowerHistory());
		clientEvent.setDescription(description);
		if (value != null) {
			clientEvent.setValue(Int32Value.of(value));
		}


		if (source != null) {
			clientEvent.setSource(getEntity(workingContext, source, playerId));
		}

		if (target != null && (targets == null || targets.isEmpty())) {
			clientEvent.setTarget(getEntity(workingContext, target, playerId));
		}

		// Deal with censoring the card for secrets
		if (card != null) {
			var cardEvent = CardEvent.newBuilder();
			if (card.getCardType() == CardType.SPELL
					&& card.isSecret()
					&& card.getOwner() != playerId
					&& !(event instanceof SecretRevealedEvent)) {
				var censoredCard = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
				if (source != null) {
					clientEvent.setSource(censoredCard);
				}
				if (target != null) {
					clientEvent.setTarget(censoredCard);
				}
				cardEvent.setCard(censoredCard);
			} else {
				cardEvent.setCard(getEntity(workingContext, card, playerId));
			}
			clientEvent.setCardEvent(cardEvent);
		}

		// Support plural targets
		if (targets != null) {
			if (targets.size() == 1) {
				clientEvent.setTarget(getEntity(workingContext, targets.get(0), playerId));
			} else {
				clientEvent.addAllTargets(targets.stream().map(e -> getEntity(workingContext, e, playerId).build()).collect(toList()));
			}
		}

		var isSourcePlayerLocal = event.getSourcePlayerId() == playerId;
		var isTargetPlayerLocal = event.getTargetPlayerId() == playerId;

		// Gracefully handle events that do not specify a source player ID
		if (event.getSourcePlayerId() == -1) {
			isSourcePlayerLocal = workingContext.getActivePlayerId() == playerId;
		}

		clientEvent
				.setIsTargetPlayerLocal(isTargetPlayerLocal)
				.setIsSourcePlayerLocal(isSourcePlayerLocal);

		// Only a handful of special cases need to be dealt with
		if (event instanceof DamageEvent) {
			var damageType = ((DamageEvent) event).getDamageType();
			clientEvent.setDamage(GameEvent.DamageMessage.newBuilder().setDamageType(damageType.stream().findFirst().orElse(DamageType.MAGICAL)));
		}

		if (event instanceof JoustEvent) {
			var joustEvent = (JoustEvent) event;
			clientEvent
					.setJoust(GameEvent.JoustMessage.newBuilder().setOpponentCard(getEntity(workingContext, joustEvent.getOpponentCard(), playerId))
							.setOwnCard(getEntity(workingContext, joustEvent.getOwnCard(), playerId)));
		}

		return clientEvent;
	}

	/**
	 * Given a context and a specification of who the local and opposing players are, generate a client game state view.
	 * This view does not leak secure information.
	 *
	 * @param workingContext A context containing the complete game state.
	 * @param local          The local player.
	 * @param opponent       The opposing player.
	 * @return A client view game state.
	 */
	public static GameState.Builder getGameState(GameContext workingContext, Player local, Player opponent) {
		List<Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible
		var localPlayerId = local.getId();

		List<Entity> localHand = new ArrayList<>();
		for (var card : local.getHand()) {
			var entity = getEntity(workingContext, card, localPlayerId);
			localHand.add(entity.build());
		}

		// Add complete information for the local hand
		entities.addAll(localHand);

		for (var battlefield : Arrays.asList(local.getMinions(), opponent.getMinions())) {
			List<Entity> minions = new ArrayList<>();
			for (var minion : battlefield) {
				var entity = getEntity(workingContext, minion, localPlayerId);
				minions.add(entity.build());
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
		}

		List<Entity> localSecrets = new ArrayList<>();
		// Add complete information for the local secrets
		for (var secret : local.getSecrets()) {
			var entity = getEntity(workingContext, secret, localPlayerId);
			localSecrets.add(entity.build());
		}

		entities.addAll(localSecrets);

		// Add limited information for opposing secrets
		List<Entity> opposingSecrets = new ArrayList<>();
		for (var secret : opponent.getSecrets()) {
			var entity = Entity.newBuilder()
					.setId(secret.getId())
					.setEntityType(EntityType.SECRET)
					.setOwner(secret.getOwner())
					.setHeroClass(secret.getSourceCard().getHeroClass())
					.setLocation(toClientLocation(secret.getEntityLocation()))
					.build();
			opposingSecrets.add(entity);
		}

		entities.addAll(opposingSecrets);

		// Get all quest information
		entities.addAll(
				Stream.concat(local.getQuests().stream(), opponent.getQuests().stream())
						.map(e -> getEntity(workingContext, e, localPlayerId).build())
						.collect(toList())
		);

		List<Entity> playerEntities = new ArrayList<>();
		// Create the heroes
		for (var player : Arrays.asList(local, opponent)) {
			var playerEntity = Entity.newBuilder()
					.setId(player.getId())
					.setName(player.getName())
					.setEntityType(EntityType.PLAYER)
					.setOwner(player.getId())
					.setLockedMana(player.getLockedMana())
					.setMaxMana(player.getMaxMana())
					.setMana(player.getMana())
					.setLocation(toClientLocation(player.getEntityLocation()))
					.setIsStartingTurn(player.hasAttribute(Attribute.STARTING_TURN))
					.setGameStarted(player.hasAttribute(Attribute.GAME_STARTED));
			playerEntities.add(playerEntity.build());
			// The heroes may have wound up in the graveyard
			var heroEntity = getEntity(workingContext, player.getHero(), localPlayerId);

			if (heroEntity == null) {
				continue;
			}

			// Include the player's mana, locked mana and max mana in the hero entity for convenience
			if (player.hasAttribute(Attribute.IMBUE)) {
				// Include the number of "charges" to render
				// Imbue is currently the only effect that takes advantage of this
				heroEntity.setCharges(Int32Value.of(player.getAttributeValue(Attribute.IMBUE)));
			}
			heroEntity
					.setMana(player.getMana())
					.setMaxMana(player.getMaxMana())
					.setLockedMana(player.getLockedMana());
			playerEntities.add(heroEntity.build());
			if (!player.getHeroPowerZone().isEmpty()) {
				var heroPowerEntity = getEntity(workingContext, player.getHeroPowerZone().get(0), localPlayerId);
				playerEntities.add(heroPowerEntity.build());
			}
			if (!player.getWeaponZone().isEmpty()) {
				var weaponEntity = getEntity(workingContext, player.getWeaponZone().get(0), localPlayerId);
				playerEntities.add(weaponEntity.build());
			}
		}

		entities.addAll(playerEntities);

		// Get local discoveries
		entities.addAll(local.getDiscoverZone().stream()
				.map(c -> getEntity(workingContext, c, localPlayerId).build())
				.collect(toList()));

		// If the opponent's discovers are uncensored, add them
		entities.addAll(opponent.getDiscoverZone().stream()
				.filter(c -> c.hasAttribute(Attribute.UNCENSORED))
				.map(c -> getEntity(workingContext, c, localPlayerId).build())
				.collect(toList()));

		// Get the heroes that may have wound up in the graveyard
		var graveyardHeroes = Stream.of(local.getGraveyard().stream(), opponent.getGraveyard().stream(), local.getRemovedFromPlay().stream(), opponent.getRemovedFromPlay().stream()).flatMap(e -> e)
				.filter(e -> e.getEntityType() == EntityType.HERO)
				.map(h -> {
					var e = getEntity(workingContext, h, localPlayerId);
					var owner = h.getOwner() == local.getId() ? local : opponent;
					e
							.setMana(owner.getMana())
							.setMaxMana(owner.getMaxMana())
							.setLockedMana(owner.getLockedMana());
					return e.build();
				})
				// Don't include heroes that have already been added
				.filter(e -> playerEntities.stream().noneMatch(v -> v.getId() == e.getId()))
				.collect(toList());
		entities.addAll(graveyardHeroes);

		// Include local set aside zone
		entities.addAll(local.getSetAsideZone().stream()
				.map(c -> getEntity(workingContext, c, localPlayerId).build())
				.collect(toList()));

		var visibleEntityIds = entities.stream().map(Entity::getId).collect(Collectors.toSet());

		entities.addAll(workingContext.getTriggers()
				.stream()
				.filter(f -> f instanceof Enchantment && visibleEntityIds.contains(f.getHostReference().getId()))
				.map(t -> getEntity(workingContext, (Enchantment) t, localPlayerId).build())
				.collect(toList()));

		// Any missing entities will get a stand-in entry
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId()))
				.map(e -> Entity.newBuilder()
						.setId(e.getId())
						.setOwner(e.getOwner())
						.setLocation(toClientLocation(e.getEntityLocation()))
						.setEntityType(e.getEntityType()).build())
				.collect(toList()));

		// Sort the entities by ID
		entities.sort(Comparator.comparingInt(Entity::getId));

		return GameState.newBuilder()
				.setIsLocalPlayerTurn(localPlayerId == workingContext.getActivePlayerId())
				.addAllEntities(entities)
				.setTurnNumber(workingContext.getTurn())
				// Always use millis consistently everywhere
				.setTimestamp(System.currentTimeMillis())
				.setTurnState(workingContext.getTurnState().toString());
	}

	/**
	 * Gets a client view of the specified game engine entity. Tries its best to not leak information given the specified
	 * user.
	 *
	 * @param workingContext A context to generate the entity view for.
	 * @param entity         The entity.
	 * @param localPlayerId  The point of view this method should use o determine which information to show the client.
	 * @return A client entity view.
	 */
	static Entity.Builder getEntity(GameContext workingContext, net.demilich.metastone.game.entities.Entity entity, int localPlayerId) {
		if (entity == null) {
			return Entity.newBuilder()
					.setId(-1);
		}

		// TODO: Shouldn't this use isAssignableFrom?
		if (entity instanceof Actor) {
			return getEntity(workingContext, (Actor) entity, localPlayerId);
		} else if (entity instanceof Card) {
			return getEntity(workingContext, (Card) entity, localPlayerId);
		} else if (entity instanceof Secret) {
			return getEntity(workingContext, (Secret) entity, localPlayerId);
		} else if (entity instanceof Quest) {
			return getEntity(workingContext, (Quest) entity, localPlayerId);
		}

		return Entity.newBuilder().setId(entity.getId())
				.setOwner(entity.getOwner())
				.setLocation(toClientLocation(entity.getEntityLocation()));
	}

	/**
	 * Gets the client's view of an actor in the game engine.
	 *
	 * @param workingContext A context to generate the entity view for.
	 * @param actor          The specified actor.
	 * @param localPlayerId  The point of view this method should use o determine which information to show the client.
	 * @return A client entity view.
	 */
	static Entity.Builder getEntity(GameContext workingContext, Actor actor, int localPlayerId) {
		if (actor == null) {
			return Entity.newBuilder()
					.setId(-1);
		}

		// For the purposes of determining whether or not the game is over, we will calculate the match result once
		if (workingContext.getStatus() == null) {
			workingContext.updateAndGetGameOver();
		}

		var owner = workingContext.getPlayer(actor.getOwner());

		var card = actor.getSourceCard();
		var entity = Entity.newBuilder()
				.setDescription(actor.getDescription(workingContext, workingContext.getPlayer(actor.getOwner())))
				.setName(actor.getName())
				.setId(actor.getId())
				.setEntityType(actor.getEntityType())
				.setCardId(card.getCardId());

		// TODO: Why are we computing extra attack?
		var extraAttack = 0;
		if (actor instanceof Minion) {
			entity.setBoardPosition(actor.getEntityLocation().getIndex());
		} else if (actor instanceof Hero) {
			entity.setArmor(Int32Value.of(actor.getArmor()));
			if (!owner.getWeaponZone().isEmpty() && owner.getWeaponZone().get(0).isActive()) {
				extraAttack += owner.getWeaponZone().get(0).getAttack();
			}
		}

		entity.setOwner(actor.getOwner());
		entity.setExtraAttack(Int32Value.of(extraAttack));
		entity.setLocation(toClientLocation(actor.getEntityLocation()));
		entity.setManaCost(Int32Value.of(card.getBaseManaCost()));
		entity.setHeroClass(card.getHeroClass());
		entity.setCardSet(Objects.toString(card.getCardSet()));
		entity.setRarity(card.getRarity());
		entity.setBaseManaCost(Int32Value.of(card.getBaseManaCost()));
		entity.setSilenced(actor.hasAttribute(Attribute.SILENCED));
		entity.setDeathrattles(actor.hasAttribute(Attribute.DEATHRATTLES));
		entity.setCardType(card.getCardType());
		var playable = actor.getOwner() == workingContext.getActivePlayerId()
				&& actor.getOwner() == localPlayerId
				&& workingContext.getStatus() == GameStatus.RUNNING
				&& actor.canAttackThisTurn(workingContext);
		entity.setPlayable(playable);
		entity.setAttack(Int32Value.of(actor.getAttack()));
		entity.setBaseAttack(Int32Value.of(actor.getBaseAttack()));
		entity.setBaseHp(Int32Value.of(actor.getBaseHp()));
		entity.setHp(Int32Value.of(actor.getHp()));
		entity.setMaxHp(Int32Value.of(actor.getMaxHp()));
		entity.setHeroClass(actor.getHeroClass());
		entity.setUnderAura(actor.hasAttribute(Attribute.AURA_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.AURA_HP_BONUS)
				|| actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_TAUNT)
				|| actor.hasAttribute(Attribute.HP_BONUS)
				|| actor.hasAttribute(Attribute.ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
		entity.setFrozen(actor.hasAttribute(Attribute.FROZEN));
		entity.setCharge(actor.hasAttribute(Attribute.CHARGE) || actor.hasAttribute(Attribute.AURA_CHARGE));
		entity.setImmune(actor.hasAttribute(Attribute.IMMUNE) || actor.hasAttribute(Attribute.AURA_IMMUNE));
		entity.setStealth(actor.hasAttribute(Attribute.STEALTH) || actor.hasAttribute(Attribute.AURA_STEALTH));
		entity.setTaunt(actor.hasAttribute(Attribute.TAUNT) || actor.hasAttribute(Attribute.AURA_TAUNT));
		entity.setDivineShield(actor.hasAttribute(Attribute.DIVINE_SHIELD));
		entity.setDeflect(actor.hasAttribute(Attribute.DEFLECT));
		entity.setEnraged(actor.hasAttribute(Attribute.ENRAGED));
		entity.setDestroyed(actor.isDestroyed());
		entity.setCannotAttack(actor.hasAttribute(Attribute.CANNOT_ATTACK) || actor.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		entity.setSpellDamage(Int32Value.of(actor.getAttributeValue(Attribute.SPELL_DAMAGE) + actor.getAttributeValue(Attribute.AURA_SPELL_DAMAGE)));
		entity.setWindfury(actor.hasAttribute(Attribute.WINDFURY) || actor.hasAttribute(Attribute.AURA_WINDFURY));
		entity.setLifesteal(actor.hasAttribute(Attribute.LIFESTEAL) || actor.hasAttribute(Attribute.AURA_LIFESTEAL));
		entity.setPoisonous(actor.hasAttribute(Attribute.POISONOUS) || actor.hasAttribute(Attribute.AURA_POISONOUS));
		entity.setSummoningSickness(actor.hasAttribute(Attribute.SUMMONING_SICKNESS));
		entity.setUntargetableBySpells(actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
		entity.setPermanent(actor.hasAttribute(Attribute.PERMANENT));
		entity.setRush(actor.hasAttribute(Attribute.RUSH) || actor.hasAttribute(Attribute.AURA_RUSH));
		entity.setTribe(actor.getRace());
		var triggers = workingContext.getLogic().getActiveTriggers(actor.getReference());
		entity.setHostsTrigger(triggers.stream().anyMatch(t -> !(t instanceof Aftermath) && !(t instanceof Aura)));
		return entity;
	}

	/**
	 * A view of a secret or quest. Censors information from opposing players if it's a secret.
	 *
	 * @param workingContext The context to generate the client view for.
	 * @param enchantment    The secret or quest entity. Any entity backed by a {@link Enchantment} is valid here.
	 * @param localPlayerId  The point of view this method should use o determine which information to show the client.
	 * @return A client entity view.
	 */
	static Entity.Builder getEntity(GameContext workingContext, Enchantment enchantment, int localPlayerId) {
		if (enchantment == null) {
			return Entity.newBuilder()
					.setId(-1);
		}

		var entity = getEntity(workingContext, enchantment.getSourceCard(), localPlayerId);
		if (enchantment instanceof Secret
				&& localPlayerId != enchantment.getOwner()) {
			// Censor information about the secret if it does not belong to the player.
			entity
					.setName("Secret")
					.setDescription("Secret")
					.setCardId("hidden");
		}
		EntityType entityType;
		if (enchantment instanceof Secret) {
			entityType = EntityType.SECRET;
		} else if (enchantment instanceof Quest) {
			entityType = EntityType.QUEST;
		} else {
			entityType = EntityType.ENCHANTMENT;
		}

		entity
				.setId(enchantment.getId())
				.setFires(Int32Value.of(enchantment.getFires()))
				.setEntityType(entityType)
				.setLocation(toClientLocation(enchantment.getEntityLocation()))
				.setOwner(enchantment.getOwner())
				.setHost(enchantment.getHostReference() != null ? enchantment.getHostReference().getId() : -1)
				.setEnchantmentType(enchantment.getClass().getSimpleName())
				.setPlayable(false);
		return entity;
	}

	/**
	 * A view of a card. This does not censor information from opposing player's--the calling method should handle the
	 * censoring.
	 *
	 * @param workingContext The context to generate the client view for.
	 * @param card           The card entity.
	 * @param localPlayerId  The point of view this method should use o determine which information to show the client.
	 * @return A client entity view.
	 */
	public static Entity.Builder getEntity(GameContext workingContext, Card card, int localPlayerId) {
		if (card == null) {
			return Entity.newBuilder()
					.setId(-1);
		}

		var entity = Entity.newBuilder()
				.setEntityType(EntityType.CARD)
				.setName(card.getName())
				.setId(card.getId())
				.setCardId(card.getCardId());
		var owner = card.getOwner();
		Player owningPlayer;
		var description = card.getDescription();
		if (owner != -1) {
			if (card.getZone() == com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.HAND
					|| card.getZone() == com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.SET_ASIDE_ZONE
					|| card.getZone() == com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones.HERO_POWER
					&& owner == localPlayerId) {
				var playable = workingContext.getLogic().canPlayCard(owner, card.getReference())
						&& card.getOwner() == workingContext.getActivePlayerId()
						&& localPlayerId == card.getOwner();
				entity.setPlayable(playable);
				entity.setManaCost(Int32Value.of(workingContext.getLogic().getModifiedManaCost(workingContext.getPlayer(owner), card)));
			} else {
				entity.setPlayable(false);
				entity.setManaCost(Int32Value.of(card.getBaseManaCost()));
			}
			owningPlayer = workingContext.getPlayer(card.getOwner());
			description = card.getDescription(workingContext, owningPlayer);
			entity.setOwner(card.getOwner());
		} else {
			entity.setPlayable(false);
			entity.setManaCost(Int32Value.of(card.getBaseManaCost()));
			entity.setOwner(localPlayerId);
			owningPlayer = Player.empty();
		}

		entity.setDescription(description.replace("$", "").replace("#", "")
				.replace("[", "").replace("]", ""));

		entity.setCardSet(Objects.toString(card.getCardSet()));
		entity.setRarity(card.getRarity());
		entity.setLocation(toClientLocation(card.getEntityLocation()));
		entity.setBaseManaCost(Int32Value.of(card.getBaseManaCost()));
		entity.setUncensored(card.hasAttribute(Attribute.UNCENSORED));
		entity.setBattlecry(card.hasAttribute(Attribute.BATTLECRY));
		entity.setDeathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
		entity.setPermanent(card.hasAttribute(Attribute.PERMANENT));
		entity.setCollectible(card.isCollectible());
		entity.setDiscarded(card.hasAttribute(Attribute.DISCARDED));
		entity.setRoasted(card.hasAttribute(Attribute.ROASTED));
		entity.setTaunt(card.hasAttribute(Attribute.TAUNT));
		entity.setHostsTrigger(card.hasTrigger() || card.hasAura() || card.hasCardCostModifier());
		var heroClass = card.getHeroClass();

		// Put the condition met glow on the card
		if (card.getZone() == Zones.HAND && entity.getPlayable()) {
			entity.setConditionMet(workingContext.getLogic().conditionMet(localPlayerId, card));
		}

		// Handles tri-class cards correctly
		if (heroClass == null) {
			heroClass = HeroClass.ANY;
		}

		entity.setHeroClass(heroClass);
		entity.setCardType(card.getCardType());
		var hostsTrigger = workingContext.getLogic().getActiveTriggers(card.getReference()).size() > 0;
		// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
		// I'd personally recommend making the glowing border effect be a custom programmable part of the .json file -doombubbles
		switch (card.getCardType()) {
			case HERO:
				// Retrieve the weapon attack
				var weapon = card.getWeapon(workingContext);
				if (weapon != null) {
					entity.setAttack(Int32Value.of(weapon.getBaseDamage()));
				}
				entity.setArmor(Int32Value.of(card.getArmor()));
				break;
			case MINION:
				entity.setAttack(Int32Value.of(card.getAttack() + card.getBonusAttack() + card.getAttributeValue(Attribute.AURA_ATTACK_BONUS)));
				entity.setBaseAttack(Int32Value.of(card.getBaseAttack()));
				entity.setBaseManaCost(Int32Value.of(card.getBaseManaCost()));
				entity.setHp(Int32Value.of(card.getHp() + card.getBonusHp() + card.getAttributeValue(Attribute.AURA_HP_BONUS)));
				entity.setBaseHp(Int32Value.of(card.getBaseHp()));
				entity.setMaxHp(Int32Value.of(card.getBaseHp() + card.getBonusHp() + card.getAttributeValue(Attribute.AURA_HP_BONUS)));
				entity.setUnderAura(card.getBonusAttack() > 0
						|| card.getBonusAttack() > 0
						|| hostsTrigger);
				entity.setTribe(card.getRace());
				// Include handbuffs from WhereverTheyAre enchantments. Also use this for other effects!
				visualizeEffectsInHand(workingContext, owningPlayer.getId(), card, entity);
				break;
			case WEAPON:
				entity.setDurability(Int32Value.of(card.getDurability()));
				entity.setHp(Int32Value.of(card.getDurability()));
				entity.setMaxHp(Int32Value.of(card.getBaseDurability() + card.getBonusDurability()));
				entity.setAttack(Int32Value.of(card.getDamage() + card.getBonusDamage()));
				entity.setUnderAura(card.getBonusDamage() > 0
						|| card.getBonusDurability() > 0
						|| hostsTrigger);
				break;
			case SPELL:
			case HERO_POWER:
				entity.setUnderAura(hostsTrigger);
				break;
			case CHOOSE_ONE:
				// TODO: Handle choose one cards
				break;
			case CLASS:
				break;
			case FORMAT:
				entity.addAllCardSets(Arrays.asList(card.getCardSets()));
				break;
		}

		return entity;
	}

	/**
	 * Converts an in-game entity location to a client view location.
	 *
	 * @param location A game engine entity location.
	 * @return A client view entity location.
	 */
	static EntityLocation toClientLocation(net.demilich.metastone.game.entities.EntityLocation location) {
		return EntityLocation.newBuilder()
				.setZone(location.getZone())
				.setIndex(location.getIndex()).build();
	}

	/**
	 * Uses information from enchantments like {@link net.demilich.metastone.game.spells.aura.BuffAura} and
	 * {@link WhereverTheyAreEnchantment} to add the appropriate hand buff stats.
	 *
	 * @param context
	 * @param playerId
	 * @param entity
	 * @param state
	 */
	static void visualizeEffectsInHand(@NotNull GameContext context, int playerId, @NotNull net.demilich.metastone.game.entities.Entity entity, @NotNull Entity.Builder state) {
		var attackBonus = 0;
		var hpBonus = 0;
		var hasTaunt = false;
		hasTaunt |= entity.hasAttribute(Attribute.CARD_TAUNT);
		for (var e : context.getTriggers()
				.stream()
				.filter(e -> !e.isExpired() && e.getOwner() == playerId && e instanceof WhereverTheyAreEnchantment)
				.map(WhereverTheyAreEnchantment.class::cast)
				.collect(Collectors.toList())) {
			List<SpellDesc> spells;
			if (e.getSpell() == null) {
				return;
			}
			if (MetaSpell.class.isAssignableFrom(e.getSpell().getDescClass())) {
				spells = e.getSpell().subSpells();
			} else {
				spells = Collections.singletonList(e.getSpell());
			}
			for (var desc : spells) {
				if (BuffSpell.class.isAssignableFrom(desc.getDescClass())) {
					attackBonus += desc.getValue(SpellArg.ATTACK_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
					hpBonus += desc.getValue(SpellArg.HP_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
					var value = desc.getValue(SpellArg.HP_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
					attackBonus += value;
					hpBonus += value;
				}
				if (AddAttributeSpell.class.isAssignableFrom(desc.getDescClass())) {
					// TODO: Add support for stuff other than Taunt
					if (desc.getAttribute() == Attribute.TAUNT) {
						hasTaunt = true;
					}
				}
			}
		}
		if (hasTaunt) {
			state.setTaunt(true);
		}
		if (attackBonus != 0) {
			state.setAttack(Int32Value.of((state.hasAttack() ? state.getAttack().getValue() : 0) + attackBonus));
		}
		if (hpBonus != 0) {
			state.setHp(Int32Value.of((state.hasHp() ? state.getHp().getValue() : 0) + hpBonus));
		}
	}

	@NotNull
	public static GameDeck getGameDeck(String userId, DecksGetResponse deckCollection) {
		var deckId = deckCollection.getCollection().getId();
		var deck = new GameDeck();
		deck.setDeckId(deckCollection.getCollection().getId());
		// TODO: Deal with how we retrieve cards here
		deck.setCards(deckCollection.getCollection().getInventoryList().stream()
				.map(cr -> Objects.requireNonNull(getDescriptionFromRecord(new CardsInDeck()
						.setCardId(cr.getEntity().getCardId())
						.setId(cr.getId())
						.setDeckId(deckId), userId, deckId)).create())
				.collect(Collectors.toCollection(CardArrayList::new)));
		deck.setFormat(ClasspathCardCatalogue.INSTANCE.getFormat(deckCollection.getCollection().getFormat()));
		deck.setHeroClass(deckCollection.getCollection().getHeroClass());
		deck.setName(deckCollection.getCollection().getName());
		return deck;
	}

	/**
	 * Compute the {@link EntityChangeSet} between two {@link GameState}s.
	 *
	 * @param gameState
	 * @return
	 */
	static EntityChangeSet visibleEntities(
			com.hiddenswitch.spellsource.common.GameState gameState) {
		// TODO: Return array of indices
		return EntityChangeSet.newBuilder().addAllIds(Stream.concat(gameState.getPlayer1().getLookup().values().stream(), gameState.getPlayer2().getLookup().values().stream())
				.sorted(Games.ENTITY_NATURAL_ORDER)
				.map(net.demilich.metastone.game.entities.Entity::getId)
				.collect(toList())).build();
	}

	/**
	 * Generates a client-readable {@link Replay} object (for use with the client replay functionality).
	 *
	 * @param originalCtx The context for which to generate a replay.
	 * @return
	 */
	static Replay replayFromGameContext(GameContext originalCtx) {
		var replay = Replay.newBuilder();
		var gameStateOld = new AtomicReference<com.hiddenswitch.spellsource.common.GameState>();
		Consumer<GameContext> augmentReplayWithCtx = (GameContext ctx) -> {
			// We record each game state by dumping the {@link GameState} objects from each player's point of
			// view and any state transitions into the replay.
			var gameStates = ReplayGameStates.newBuilder();
			var gameStateFirst = getGameState(ctx, ctx.getPlayer1(), ctx.getPlayer2());
			var gameStateSecond = getGameState(ctx, ctx.getPlayer2(), ctx.getPlayer1());
			gameStates.setFirst(gameStateFirst);
			gameStates.setSecond(gameStateSecond);
			replay.addGameStates(gameStates);

			var gameStateNew = ctx.getGameState();
			var delta = ReplayDeltas.newBuilder();
			delta.setForward(visibleEntities(gameStateNew));
			if (gameStateOld.get() != null) {
				// NOTE: It is illegal to rewind past the beginning of the game, so the very first delta need not have
				// backward populated.
				delta.setBackward(visibleEntities(gameStateOld.get()));
			}
			replay.addDeltas(delta);

			gameStateOld.set(ctx.getGameStateCopy());
		};

		try {
			// Replay the game from a trace while capturing the {@link Replay} object.
			var replayCtx = originalCtx.getTrace().replayContext(
					false,
					augmentReplayWithCtx
			);

			// Append the final game states / deltas.
			augmentReplayWithCtx.accept(replayCtx);
		} catch (Throwable any) {
			Tracing.error(any);
		}

		return replay.build();
	}

	@NotNull
	public static <T extends Message> T fromStringMap(T existing, String prefix, String separator, Map<String, String> map) {
		// process environment variables into jackson object
		prefix = prefix.toLowerCase(Locale.ROOT);
		// first decode into a map
		var target = new LinkedHashMap<String, Object>();
		for (var kv : map.entrySet()) {
			var path = kv.getKey().toLowerCase(Locale.ROOT);
			if (!path.startsWith(prefix + separator)) {
				continue;
			}

			var parts = path.split(separator);
			// only contains spellsource_
			if (parts.length == 1) {
				continue;
			}

			var current = target;
			for (var i = 1; i < parts.length; i++) {
				var key = parts[i];

				// set the value if this is the last path message
				if (i == parts.length - 1) {
					var value = kv.getValue();
					var parsedValue = new Object();
					if (value.length() > 0) {
						var c = value.charAt(0);
						if (Character.isDigit(c)) {
							if (value.contains(",") || value.contains(".")) {
								parsedValue = Double.parseDouble(value);
							} else {
								var longValue = Long.parseLong(value);
								if (longValue < Integer.MAX_VALUE) {
									parsedValue = (int) longValue;
								} else {
									parsedValue = longValue;
								}
							}
						} else {
							if (value.toLowerCase(Locale.ROOT).equals("false")) {
								parsedValue = false;
							} else if (value.toLowerCase(Locale.ROOT).equals("true")) {
								parsedValue = true;
							} else {
								parsedValue = value;
							}
						}
					}
					current.put(key, parsedValue);
					continue;
				}

				// descending into sub object
				// arrays not currently supported
				if (!current.containsKey(key)) {
					var descended = new LinkedHashMap<String, Object>();
					current.put(key, descended);
					current = descended;
					continue;
				}

				if (current.get(key) instanceof LinkedHashMap) {
					@SuppressWarnings("unchecked")
					var x = (LinkedHashMap<String, Object>) current.get(key);
					current = x;
					continue;
				}

				// this is a configuration error and the application should probably exit
				LOGGER.error("fromStringMap: Invalid key " + key + ", skipping");
			}
		}

		try {
			// decode into the protobuf to get the benefit of field insensitivity
			var toMerge = caseInsensitiveMapper.convertValue(target, Hiddenswitch.ServerConfiguration.class);
			// merge
			@SuppressWarnings("unchecked")
			var merged = (T) existing.toBuilder().mergeFrom(toMerge).build();
			return merged;
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("make sure to call Serialization.configureSerialization()", ex);
		}
	}
}
