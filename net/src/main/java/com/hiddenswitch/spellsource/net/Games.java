package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.ActionType;
import com.hiddenswitch.spellsource.client.models.CardEvent;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.ClusteredGames;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.impl.Rpc;
import io.vertx.core.Verticle;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Actor;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.AddAttributeSpell;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.WhereverTheyAreEnchantment;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.client.models.EntityType.*;
import static java.util.stream.Collectors.toList;


/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 * <p>
 * Various static methods convert game data into a format the Unity3D client can understand.
 */
public interface Games extends Verticle {
	Logger LOGGER = LoggerFactory.getLogger(Games.class);
	long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;
	String GAMES_PLAYERS_MAP = "Games/players";
	String GAMES = "games";
	Comparator<net.demilich.metastone.game.entities.Entity> ENTITY_NATURAL_ORDER = Comparator
			.comparing(net.demilich.metastone.game.entities.Entity::getZone)
			.thenComparingInt(net.demilich.metastone.game.entities.Entity::getIndex);

	/**
	 * Creates a new instance of the service that maintains a list of running games.
	 *
	 * @return A games instance.
	 */
	static Games create() {
		return new ClusteredGames();
	}

	/**
	 * Get an entity representing a censored secret card.
	 *
	 * @param id        The card's entity ID
	 * @param owner     The secret's owner
	 * @param location  The secret'slocation
	 * @param heroClass The hero class of the secret
	 * @return A censored secret card.
	 */
	static com.hiddenswitch.spellsource.client.models.Entity getCensoredCard(int id, int owner, net.demilich.metastone.game.entities.EntityLocation location, String heroClass) {
		return new com.hiddenswitch.spellsource.client.models.Entity()
				.cardId("hidden")
				.entityType(CARD)
				.description("A secret! This card will be revealed when a certain action occurs.")
				.name("Secret")
				.id(id)
				.owner(owner)
				.cardType(CardType.SPELL)
				.heroClass(heroClass)
				.l(toClientLocation(location));
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
				ActionKey actionKey = (ActionKey) o;
				return sourceReference == actionKey.sourceReference &&
						actionType == actionKey.actionType;
			}

			@Override
			public int hashCode() {
				return com.google.common.base.Objects.hashCode(sourceReference, actionType);
			}
		}

		Map<ActionKey, List<GameAction>> actionMap = actions.stream()
				.unordered()
				.collect(Collectors.groupingBy(ActionKey::new));
		EntityZone<Minion> friendlyMinions = workingContext.getPlayer(playerId).getMinions();
		CardZone discovers = workingContext.getPlayer(playerId).getDiscoverZone();
		GameActions clientActions = new GameActions()
				.all(
						actionMap.entrySet()
								.stream()
								.unordered()
								.flatMap(kv -> {
									if (kv.getKey().actionType == ActionType.SUMMON) {
										return Stream.of(new SpellAction()
												.sourceId(kv.getKey().sourceReference)
												.actionType(kv.getKey().actionType)
												.targetKeyToActions(kv.getValue().stream().map(ga -> new TargetActionPair()
														.action(ga.getId())
														.friendlyBattlefieldIndex(friendlyMinions.stream().filter(m -> Objects.equals(m.getReference(), ga.getTargetReference())).map(Minion::getIndex).findFirst().orElse(friendlyMinions.size()))
														.target((ga.getTargetReference() == null || Objects.equals(ga.getTargetReference(), EntityReference.NONE)) ? -1 : ga.getTargetReference().getId())).collect(toList())));
									} else if (kv.getKey().actionType == ActionType.DISCOVER) {
										// Find the corresponding cards in the discover zone
										kv.getValue().sort(Comparator.comparingInt(GameAction::getId));
										return IntStream.range(0, discovers.size())
												.mapToObj(i -> {
													GameAction sourceAction = kv.getValue().get(i);
													return new SpellAction()
															.sourceId(discovers.get(i).getId())
															.action(sourceAction.getId())
															.actionType(ActionType.DISCOVER);
												});
									} else if (kv.getValue().get(0).getTargetRequirement() == TargetSelection.NONE) {
										GameAction ga = kv.getValue().get(0);
										return Stream.of(new SpellAction()
												.sourceId(kv.getKey().sourceReference)
												.action(ga.getId())
												.entity(ga instanceof net.demilich.metastone.game.entities.HasCard ?
														getEntity(workingContext, ((net.demilich.metastone.game.entities.HasCard) ga).getSourceCard(), playerId) : null)
												.description(ga.getDescription(workingContext, playerId))
												.actionType(ActionType.valueOf(ga.getActionType().name())));
									} else {
										return Stream.of(new SpellAction()
												.sourceId(kv.getKey().sourceReference)
												.actionType(kv.getKey().actionType)
												.targetKeyToActions(kv.getValue().stream().map(ga -> new TargetActionPair()
														.action(ga.getId())
														.target((ga.getTargetReference() == null || Objects.equals(ga.getTargetReference(), EntityReference.NONE)) ? -1 : ga.getTargetReference().getId())).collect(toList())));
									}
								})
								.collect(toList())
				);

		// Add all the action indices for compatibility purposes
		clientActions.compatibility(actions.stream()
				.map(GameAction::getId)
				.collect(toList()));

		return clientActions;
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
	static com.hiddenswitch.spellsource.client.models.GameEvent getClientEvent(net.demilich.metastone.game.events.GameEvent event, int playerId) {
		com.hiddenswitch.spellsource.client.models.GameEvent clientEvent = new com.hiddenswitch.spellsource.client.models.GameEvent();

		clientEvent.eventType(event.getEventType());

		GameContext workingContext = event.getGameContext().clone();
		var source = event.getSource(workingContext);
		var target = event.getTarget();
		var value = event instanceof HasValue ? ((HasValue) event).getValue() : null;
		var card = event instanceof HasCard ? ((HasCard) event).getSourceCard() : null;
		var description = event.isPowerHistory() ? event.getDescription(workingContext, playerId) : null;

		clientEvent
				.description(description)
				.value(value);

		// Deal with censoring the card for secrets
		if (card != null) {
			var cardEvent = new CardEvent();
			clientEvent.cardEvent(cardEvent);
			if (card.getCardType() == CardType.SPELL
					&& card.isSecret()
					&& card.getOwner() != playerId
					&& !(event instanceof SecretRevealedEvent)) {
				var censoredCard = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
				cardEvent.card(censoredCard);
				if (source != null) {
					clientEvent.source(censoredCard);
				}
				if (target != null) {
					clientEvent.target(censoredCard);
				}
			} else {
				cardEvent.card(getEntity(workingContext, card, playerId));
			}
		}

		if (source != null && clientEvent.getSource() == null) {
			clientEvent.source(getEntity(workingContext, source, playerId));
		}

		if (target != null && clientEvent.getTarget() == null) {
			clientEvent.target(getEntity(workingContext, target, playerId));
		}

		clientEvent
				.isTargetPlayerLocal(event.getTargetPlayerId() == playerId)
				.isSourcePlayerLocal(event.getSourcePlayerId() == playerId);

		// Only a handful of special cases need to be dealt with
		if (event instanceof DamageEvent) {
			clientEvent.damage(new GameEventDamage().damageType(((DamageEvent) event).getDamageType()));
		}

		if (event instanceof JoustEvent) {
			var joustEvent = (JoustEvent) event;
			clientEvent
					.joust(new GameEventJoust().opponentCard(getEntity(workingContext, joustEvent.getOpponentCard(), playerId))
							.ownCard(getEntity(workingContext, joustEvent.getOwnCard(), playerId)));
		}

		return clientEvent;
	}

	/**
	 * Retrieves the current connections by Game ID
	 *
	 * @return A map.
	 */
	@Suspendable
	static SuspendableMap<GameId, CreateGameSessionResponse> getConnections() throws SuspendExecution {
		return SuspendableMap.getOrCreate("Games/connections");
	}

	/**
	 * Retrieves the current game a player is part of.
	 *
	 * @return
	 * @throws SuspendExecution
	 */
	static SuspendableMap<UserId, GameId> getUsersInGames() throws SuspendExecution {
		return SuspendableMap.getOrCreate(GAMES_PLAYERS_MAP);
	}

	/**
	 * Creates a match without entering a queue entry between two users.
	 *
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MatchCreateResponse createGame(ConfigurationRequest request) throws SuspendExecution, InterruptedException {
		Matchmaking.LOGGER.debug("createMatch: Creating match for request {}", request);

		Games gamesService = Rpc.connect(Games.class).sync();
		return new MatchCreateResponse(gamesService.createGameSession(request));
	}

	/**
	 * Specifies the number of milliseconds to wait for players to connect to a {@link
	 * com.hiddenswitch.spellsource.net.impl.util.ServerGameContext} that was just created.
	 *
	 * @return
	 */
	static long getDefaultConnectionTime() {
		return 12000L;
	}

	/**
	 * Creates a game session on this instance. Returns once the game is ready to receive first messages
	 *
	 * @param request Information needed to start a game.
	 * @return Information for the users to connect to the game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	CreateGameSessionResponse createGameSession(ConfigurationRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Given a context and a specification of who the local and opposing players are, generate a client game state view.
	 * This view does not leak secure information.
	 *
	 * @param workingContext A context containing the complete game state.
	 * @param local          The local player.
	 * @param opponent       The opposing player.
	 * @return A client view game state.
	 */
	static GameState getGameState(GameContext workingContext, Player local, Player opponent) {
		List<com.hiddenswitch.spellsource.client.models.Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible
		int localPlayerId = local.getId();

		List<com.hiddenswitch.spellsource.client.models.Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, localPlayerId);
			localHand.add(entity);
		}

		// Add complete information for the local hand
		entities.addAll(localHand);

		for (EntityZone<Minion> battlefield : Arrays.asList(local.getMinions(), opponent.getMinions())) {
			List<com.hiddenswitch.spellsource.client.models.Entity> minions = new ArrayList<>();
			for (Minion minion : battlefield) {
				com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, minion, localPlayerId);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
		}

		List<com.hiddenswitch.spellsource.client.models.Entity> localSecrets = new ArrayList<>();
		// Add complete information for the local secrets
		for (Secret secret : local.getSecrets()) {
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, secret, localPlayerId);
			localSecrets.add(entity);
		}

		entities.addAll(localSecrets);

		// Add limited information for opposing secrets
		List<com.hiddenswitch.spellsource.client.models.Entity> opposingSecrets = new ArrayList<>();
		for (Secret secret : opponent.getSecrets()) {
			com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
					.id(secret.getId())
					.entityType(SECRET)
					.owner(secret.getOwner())
					.heroClass(secret.getSourceCard().getHeroClass())
					.l(Games.toClientLocation(secret.getEntityLocation()));
			opposingSecrets.add(entity);
		}

		entities.addAll(opposingSecrets);

		// Get all quest information
		entities.addAll(
				Stream.concat(local.getQuests().stream(), opponent.getQuests().stream())
						.map(e -> getEntity(workingContext, e, localPlayerId))
						.collect(toList())
		);

		List<com.hiddenswitch.spellsource.client.models.Entity> playerEntities = new ArrayList<>();
		// Create the heroes
		for (Player player : Arrays.asList(local, opponent)) {
			com.hiddenswitch.spellsource.client.models.Entity playerEntity = new com.hiddenswitch.spellsource.client.models.Entity()
					.id(player.getId())
					.name(player.getName())
					.entityType(PLAYER)
					.owner(player.getId())
					.lockedMana(player.getLockedMana())
					.maxMana(player.getMaxMana())
					.mana(player.getMana())
					.l(Games.toClientLocation(player.getEntityLocation()))
					.isStartingTurn(player.hasAttribute(Attribute.STARTING_TURN))
					.gameStarted(player.hasAttribute(Attribute.GAME_STARTED));
			playerEntities.add(playerEntity);
			// The heroes may have wound up in the graveyard
			com.hiddenswitch.spellsource.client.models.Entity heroEntity = getEntity(workingContext, player.getHero(), localPlayerId);

			if (heroEntity == null) {
				continue;
			}

			// Include the player's mana, locked mana and max mana in the hero entity for convenience
			heroEntity
					.mana(player.getMana())
					.maxMana(player.getMaxMana())
					.lockedMana(player.getLockedMana());
			playerEntities.add(heroEntity);
			if (!player.getHeroPowerZone().isEmpty()) {
				com.hiddenswitch.spellsource.client.models.Entity heroPowerEntity = getEntity(workingContext, player.getHeroPowerZone().get(0), localPlayerId);
				playerEntities.add(heroPowerEntity);
			}
			if (!player.getWeaponZone().isEmpty()) {
				com.hiddenswitch.spellsource.client.models.Entity weaponEntity = getEntity(workingContext, player.getWeaponZone().get(0), localPlayerId);
				playerEntities.add(weaponEntity);
			}
		}

		entities.addAll(playerEntities);

		// Get local discoveries
		entities.addAll(local.getDiscoverZone().stream()
				.map(c -> getEntity(workingContext, c, localPlayerId))
				.collect(toList()));

		// If the opponent's discovers are uncensored, add them
		entities.addAll(opponent.getDiscoverZone().stream()
				.filter(c -> c.hasAttribute(Attribute.UNCENSORED))
				.map(c -> getEntity(workingContext, c, localPlayerId))
				.collect(toList()));

		// Get the heroes that may have wound up in the graveyard
		List<Entity> graveyardHeroes = Stream.of(local.getGraveyard().stream(), opponent.getGraveyard().stream(), local.getRemovedFromPlay().stream(), opponent.getRemovedFromPlay().stream()).flatMap(e -> e)
				.filter(e -> e.getEntityType() == HERO)
				.map(h -> {
					Entity e = getEntity(workingContext, h, localPlayerId);
					Player owner = h.getOwner() == local.getId() ? local : opponent;
					e
							.mana(owner.getMana())
							.maxMana(owner.getMaxMana())
							.lockedMana(owner.getLockedMana());
					return e;
				})
				// Don't include heroes that have already been added
				.filter(e -> playerEntities.stream().noneMatch(v -> v.getId().equals(e.getId())))
				.collect(toList());
		entities.addAll(graveyardHeroes);

		// Include local set aside zone
		entities.addAll(local.getSetAsideZone().stream()
				.map(c -> getEntity(workingContext, c, localPlayerId))
				.collect(toList()));

		var visibleEntityIds = entities.stream().map(com.hiddenswitch.spellsource.client.models.Entity::getId).collect(Collectors.toSet());

		// For now, do not send enchantments data
		/*
		entities.addAll(workingContext.getTriggers()
				.stream()
				.filter(f -> f instanceof Enchantment && visibleEntityIds.contains(f.getHostReference().getId()))
				.map(t -> getEntity(workingContext, (Enchantment) t, localPlayerId))
				.collect(toList()));
		*/

		// Any missing entities will get a stand-in entry
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId())).map(e -> new com.hiddenswitch.spellsource.client.models.Entity()
				.id(e.getId())
				.owner(e.getOwner())
				.l(toClientLocation(e.getEntityLocation()))
				.entityType(valueOf(e.getEntityType().toString()))).collect(toList()));

		// Sort the entities by ID
		entities.sort(Comparator.comparingInt(Entity::getId));

		return new GameState()
				.isLocalPlayerTurn(localPlayerId == workingContext.getActivePlayerId())
				.entities(entities)
				.turnNumber(workingContext.getTurn())
				// Always use millis consistently everywhere
				.timestamp(System.currentTimeMillis())
				.turnState(workingContext.getTurnState().toString());
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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(GameContext workingContext, net.demilich.metastone.game.entities.Entity entity, int localPlayerId) {
		if (entity == null) {
			return null;
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

		return null;
	}

	/**
	 * Gets the client's view of an actor in the game engine.
	 *
	 * @param workingContext A context to generate the entity view for.
	 * @param actor          The specified actor.
	 * @param localPlayerId  The point of view this method should use o determine which information to show the client.
	 * @return A client entity view.
	 */
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(GameContext workingContext, Actor actor, int localPlayerId) {
		if (actor == null) {
			return null;
		}

		// For the purposes of determining whether or not the game is over, we will calculate the match result once
		if (workingContext.getStatus() == null) {
			workingContext.updateAndGetGameOver();
		}

		var owner = workingContext.getPlayer(actor.getOwner());

		Card card = actor.getSourceCard();
		com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
				.description(actor.getDescription(workingContext, workingContext.getPlayer(actor.getOwner())))
				.name(actor.getName())
				.id(actor.getId())
				.entityType(actor.getEntityType())
				.cardId(card.getCardId());

		var extraAttack = 0;
		if (actor instanceof Minion) {
			entity.boardPosition(actor.getEntityLocation().getIndex());
		} else if (actor instanceof Hero) {
			entity.armor(actor.getArmor());
			if (!owner.getWeaponZone().isEmpty() && owner.getWeaponZone().get(0).isActive()) {
				extraAttack += owner.getWeaponZone().get(0).getAttack();
			}
		}

		entity.owner(actor.getOwner());
		entity.l(Games.toClientLocation(actor.getEntityLocation()));
		entity.manaCost(card.getBaseManaCost());
		entity.heroClass(card.getHeroClass());
		entity.cardSet(Objects.toString(card.getCardSet()));
		entity.rarity(card.getRarity());
		entity.baseManaCost(card.getBaseManaCost());
		entity.silenced(actor.hasAttribute(Attribute.SILENCED));
		entity.deathrattles(actor.hasAttribute(Attribute.DEATHRATTLES));
		boolean playable = actor.getOwner() == workingContext.getActivePlayerId()
				&& actor.getOwner() == localPlayerId
				&& workingContext.getStatus() == GameStatus.RUNNING
				&& actor.canAttackThisTurn(workingContext);
		entity.playable(playable);
		entity.attack(actor.getAttack());
		entity.baseAttack(actor.getBaseAttack());
		entity.baseHp(actor.getBaseHp());
		entity.hp(actor.getHp());
		entity.maxHp(actor.getMaxHp());
		entity.heroClass(actor.getHeroClass());
		entity.underAura(actor.hasAttribute(Attribute.AURA_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.AURA_HP_BONUS)
				|| actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_TAUNT)
				|| actor.hasAttribute(Attribute.HP_BONUS)
				|| actor.hasAttribute(Attribute.ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
		entity.frozen(actor.hasAttribute(Attribute.FROZEN));
		entity.charge(actor.hasAttribute(Attribute.CHARGE) || actor.hasAttribute(Attribute.AURA_CHARGE));
		entity.immune(actor.hasAttribute(Attribute.IMMUNE) || actor.hasAttribute(Attribute.AURA_IMMUNE));
		entity.stealth(actor.hasAttribute(Attribute.STEALTH) || actor.hasAttribute(Attribute.AURA_STEALTH));
		entity.taunt(actor.hasAttribute(Attribute.TAUNT) || actor.hasAttribute(Attribute.AURA_TAUNT));
		entity.divineShield(actor.hasAttribute(Attribute.DIVINE_SHIELD));
		entity.deflect(actor.hasAttribute(Attribute.DEFLECT));
		entity.enraged(actor.hasAttribute(Attribute.ENRAGED));
		entity.destroyed(actor.hasAttribute(Attribute.DESTROYED));
		entity.cannotAttack(actor.hasAttribute(Attribute.CANNOT_ATTACK) || actor.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		entity.spellDamage(actor.getAttributeValue(Attribute.SPELL_DAMAGE) + actor.getAttributeValue(Attribute.AURA_SPELL_DAMAGE));
		entity.windfury(actor.hasAttribute(Attribute.WINDFURY) || actor.hasAttribute(Attribute.AURA_WINDFURY));
		entity.lifesteal(actor.hasAttribute(Attribute.LIFESTEAL) || actor.hasAttribute(Attribute.AURA_LIFESTEAL));
		entity.poisonous(actor.hasAttribute(Attribute.POISONOUS) || actor.hasAttribute(Attribute.AURA_POISONOUS));
		entity.summoningSickness(actor.hasAttribute(Attribute.SUMMONING_SICKNESS));
		entity.untargetableBySpells(actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
		entity.permanent(actor.hasAttribute(Attribute.PERMANENT));
		entity.rush(actor.hasAttribute(Attribute.RUSH) || actor.hasAttribute(Attribute.AURA_RUSH));
		entity.tribe(actor.getRace());
		List<Trigger> triggers = workingContext.getLogic().getActiveTriggers(actor.getReference());
		entity.hostsTrigger(triggers.size() > 0);
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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(GameContext workingContext, Enchantment enchantment, int localPlayerId) {
		if (enchantment == null) {
			return null;
		}

		com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, enchantment.getSourceCard(), localPlayerId);
		if (enchantment instanceof Secret
				&& localPlayerId != enchantment.getOwner()) {
			// Censor information about the secret if it does not belong to the player.
			entity
					.name("Secret")
					.description("Secret")
					.cardId("hidden");
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
				.id(enchantment.getId())
				.fires(enchantment.getFires())
				.entityType(entityType)
				.l(Games.toClientLocation(enchantment.getEntityLocation()))
				.owner(enchantment.getOwner())
				.playable(false);
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
	@Suspendable
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(GameContext workingContext, Card card, int localPlayerId) {
		if (card == null) {
			return null;
		}

		com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
				.entityType(CARD)
				.name(card.getName())
				.id(card.getId())
				.cardId(card.getCardId());
		int owner = card.getOwner();
		Player owningPlayer;
		String description = card.getDescription();
		if (owner != -1) {
			if (card.getZone() == Zones.HAND
					|| card.getZone() == Zones.SET_ASIDE_ZONE
					|| card.getZone() == Zones.HERO_POWER
					&& owner == localPlayerId) {
				boolean playable = workingContext.getLogic().canPlayCard(owner, card.getReference())
						&& card.getOwner() == workingContext.getActivePlayerId()
						&& localPlayerId == card.getOwner();
				entity.playable(playable);
				entity.manaCost(workingContext.getLogic().getModifiedManaCost(workingContext.getPlayer(owner), card));
			} else {
				entity.playable(false);
				entity.manaCost(card.getBaseManaCost());
			}
			owningPlayer = workingContext.getPlayer(card.getOwner());

			description = card.getDescription(workingContext, owningPlayer);
		} else {
			entity.playable(false);
			entity.manaCost(card.getBaseManaCost());
			entity.owner(localPlayerId);
			owningPlayer = Player.empty();
		}

		entity.description(description.replace("$", "").replace("#", "")
				.replace("[", "").replace("]", ""));

		entity.owner(card.getOwner());
		entity.cardSet(Objects.toString(card.getCardSet()));
		entity.rarity(card.getRarity());
		entity.l(Games.toClientLocation(card.getEntityLocation()));
		entity.baseManaCost(card.getBaseManaCost());
		entity.uncensored(card.hasAttribute(Attribute.UNCENSORED));
		entity.battlecry(card.hasAttribute(Attribute.BATTLECRY));
		entity.deathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
		entity.permanent(card.hasAttribute(Attribute.PERMANENT));
		entity.collectible(card.isCollectible());
		entity.discarded(card.hasAttribute(Attribute.DISCARDED));
		entity.roasted(card.hasAttribute(Attribute.ROASTED));
		// TODO: A little too underperformant so we're going to skip this
		// entityState.conditionMet(workingContext.getLogic().conditionMet(localPlayerId, card));
		String heroClass = card.getHeroClass();

		// Handles tri-class cards correctly
		if (heroClass == null) {
			heroClass = HeroClass.ANY;
		}

		entity.heroClass(heroClass);
		entity.cardType(card.getCardType());
		boolean hostsTrigger = workingContext.getLogic().getActiveTriggers(card.getReference()).size() > 0;
		// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
		// I'd personally recommend making the glowing border effect be a custom programmable part of the .json file -doombubbles
		switch (card.getCardType()) {
			case HERO:
				// Retrieve the weapon attack
				Card weapon = card.getWeapon();
				if (weapon != null) {
					entity.attack(weapon.getBaseDamage());
				}
				entity.armor(card.getArmor());
				break;
			case MINION:
				entity.attack(card.getAttack() + card.getBonusAttack() + card.getAttributeValue(Attribute.AURA_ATTACK_BONUS));
				entity.baseAttack(card.getBaseAttack());
				entity.baseManaCost(card.getBaseManaCost());
				entity.hp(card.getHp() + card.getBonusHp() + card.getAttributeValue(Attribute.AURA_HP_BONUS));
				entity.baseHp(card.getBaseHp());
				entity.maxHp(card.getBaseHp() + card.getBonusHp() + card.getAttributeValue(Attribute.AURA_HP_BONUS));
				entity.underAura(card.getBonusAttack() > 0
						|| card.getBonusAttack() > 0
						|| hostsTrigger);
				entity.tribe(card.getRace());
				// Include handbuffs from WhereverTheyAre enchantments. Also use this for other effects!
				visualizeEffectsInHand(workingContext, owningPlayer.getId(), card, entity);
				break;
			case WEAPON:
				entity.durability(card.getDurability());
				entity.hp(card.getDurability());
				entity.maxHp(card.getBaseDurability() + card.getBonusDurability());
				entity.attack(card.getDamage() + card.getBonusDamage());
				entity.underAura(card.getBonusDamage() > 0
						|| card.getBonusDurability() > 0
						|| hostsTrigger);
				break;
			case SPELL:
			case HERO_POWER:
				int damage = 0;
				int spellpowerDamage = 0;
				SpellDesc spell = card.getSpell();

				// Could be a choose-one hero power card
				if (spell == null) {
					break;
				}

				/*
				if (card.getZone() == Zones.HAND
						&& DamageSpell.class.isAssignableFrom(spell.getDescClass())
						&& owningPlayer != null) {

					Minion oneOne = CardCatalogue.getCardById(CardCatalogue.getOneOneNeutralMinionCardId()).summon();
					oneOne.setId(65535);
					damage = DamageSpell.getDamage(workingContext, owningPlayer, card.getSpell(), card, oneOne);
					spellpowerDamage = workingContext.getLogic().applySpellpower(owningPlayer, card, damage);
				}
				*/
				entity.underAura(spellpowerDamage > damage
						|| hostsTrigger);
				entity.spellDamage(spellpowerDamage);
				break;
			case CHOOSE_ONE:
				// TODO: Handle choose one cards
				break;
			case CLASS:
				entity.blackText(card.isBlackText());
				if (card.getColor() != null) {
					entity.color(Arrays.asList(card.getColor()[0] / 255f, card.getColor()[1] / 255f, card.getColor()[2] / 255f));
				}
				break;
			case FORMAT:
				entity.cardSets(Arrays.asList(card.getCardSets()));
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
	static com.hiddenswitch.spellsource.client.models.EntityLocation toClientLocation(net.demilich.metastone.game.entities.EntityLocation location) {
		return new com.hiddenswitch.spellsource.client.models.EntityLocation()
				.z(com.hiddenswitch.spellsource.client.models.EntityLocation.ZEnum.valueOf(location.getZone().getSerialized()))
				.i(location.getIndex());
	}

	/**
	 * Gets the default no activity timeout as configured across the cluster. This timeout is used to determine when to
	 * end games that have received no actions from either client connected to them.
	 *
	 * @return A value in milliseconds of how long to wait for an action from a client before marking a game as over due
	 * to disconnection.
	 */
	static long getDefaultNoActivityTimeout() {
		return Long.parseLong(System.getProperties().getProperty("games.defaultNoActivityTimeout", Long.toString(Games.DEFAULT_NO_ACTIVITY_TIMEOUT)));
	}

	/**
	 * Compute the {@link EntityChangeSet} between two {@link GameState}s.
	 *
	 * @param gameStateNew
	 * @return
	 */
	static EntityChangeSet computeChangeSet(
			com.hiddenswitch.spellsource.common.GameState gameStateNew) {
		// TODO: Return array of indices
		return new EntityChangeSet().ids(Stream.concat(gameStateNew.getPlayer1().getLookup().values().stream(), gameStateNew.getPlayer2().getLookup().values().stream())
				.sorted(ENTITY_NATURAL_ORDER)
				.map(net.demilich.metastone.game.entities.Entity::getId)
				.collect(Collectors.toList()));
	}

	/**
	 * Generates a client-readable {@link Replay} object (for use with the client replay functionality).
	 *
	 * @param originalCtx The context for which to generate a replay.
	 * @return
	 */
	static Replay replayFromGameContext(GameContext originalCtx) {
		Replay replay = new Replay();
		AtomicReference<com.hiddenswitch.spellsource.common.GameState> gameStateOld = new AtomicReference<>();
		Consumer<GameContext> augmentReplayWithCtx = (GameContext ctx) -> {
			// We record each game state by dumping the {@link GameState} objects from each player's point of
			// view and any state transitions into the replay.
			ReplayGameStates gameStates = new ReplayGameStates();
			GameState gameStateFirst = getGameState(ctx, ctx.getPlayer1(), ctx.getPlayer2());
			// NOTE: It seems difficult to get Swagger codegen to actually respect a default empty array so instead we
			// set one manually.
			gameStateFirst.setPowerHistory(new ArrayList<>());
			GameState gameStateSecond = getGameState(ctx, ctx.getPlayer2(), ctx.getPlayer1());
			gameStateSecond.setPowerHistory(new ArrayList<>());
			gameStates.first(gameStateFirst);
			gameStates.second(gameStateSecond);
			replay.addGameStatesItem(gameStates);

			com.hiddenswitch.spellsource.common.GameState gameStateNew = ctx.getGameState();
			ReplayDeltas delta = new ReplayDeltas();
			delta.forward(computeChangeSet(gameStateNew));
			if (gameStateOld.get() != null) {
				// NOTE: It is illegal to rewind past the beginning of the game, so the very first delta need not have
				// backward populated.
				delta.backward(computeChangeSet(gameStateOld.get()));
			}
			replay.addDeltasItem(delta);

			gameStateOld.set(ctx.getGameStateCopy());
		};

		try {
			// Replay the game from a trace while capturing the {@link Replay} object.
			GameContext replayCtx = originalCtx.getTrace().replayContext(
					false,
					augmentReplayWithCtx
			);

			// Append the final game states / deltas.
			augmentReplayWithCtx.accept(replayCtx);
		} catch (Throwable any) {
			Tracing.error(any);
		}

		return replay;
	}

	/**
	 * Uses information from enchantments like {@link net.demilich.metastone.game.spells.aura.BuffAura} and {@link
	 * net.demilich.metastone.game.spells.trigger.WhereverTheyAreEnchantment} to add the appropriate hand buff stats.
	 *
	 * @param context
	 * @param playerId
	 * @param entity
	 * @param state
	 */
	static void visualizeEffectsInHand(@NotNull GameContext context, int playerId, @NotNull net.demilich.metastone.game.entities.Entity entity, @NotNull Entity state) {
		int attackBonus = 0;
		int hpBonus = 0;
		boolean hasTaunt = false;
		hasTaunt |= entity.hasAttribute(Attribute.CARD_TAUNT);
		for (WhereverTheyAreEnchantment e : context.getTriggers()
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
			for (SpellDesc desc : spells) {
				if (BuffSpell.class.isAssignableFrom(desc.getDescClass())) {
					attackBonus += desc.getValue(SpellArg.ATTACK_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
					hpBonus += desc.getValue(SpellArg.HP_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
					int value = desc.getValue(SpellArg.HP_BONUS, context, context.getPlayer(playerId), entity, context.getPlayer(playerId), 0);
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
			state.taunt(true);
		}
		if (attackBonus != 0) {
			state.setAttack(state.getAttack() + attackBonus);
		}
		if (hpBonus != 0) {
			state.setHp(state.getHp() + hpBonus);
		}
	}
}
