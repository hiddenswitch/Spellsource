package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.SuspendablePump;
import com.hiddenswitch.spellsource.impl.ClusteredGamesImpl;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.EventBusWriter;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.SharedData;
import com.hiddenswitch.spellsource.util.SuspendableAsyncMap;
import com.hiddenswitch.spellsource.util.SuspendableMap;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.utils.Attribute;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.fiberHandler;
import static java.util.stream.Collectors.toList;


/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 */
public interface Games extends Verticle {
	Logger LOGGER = LoggerFactory.getLogger(Games.class);
	String WEBSOCKET_PATH = "games";
	long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;

	static Games create() {
		return new ClusteredGamesImpl();
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
	static com.hiddenswitch.spellsource.client.models.Entity getCensoredCard(int id, int owner, net.demilich.metastone.game.entities.EntityLocation location, HeroClass heroClass) {
		return new com.hiddenswitch.spellsource.client.models.Entity()
				.cardId("hidden")
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.CARD)
				.description("A secret! This card will be revealed when a certain action occurs.")
				.name("Secret")
				.id(id)
				.state(new EntityState()
						.owner(owner)
						.cardType(EntityState.CardTypeEnum.SPELL)
						.heroClass(heroClass.toString())
						.location(toClientLocation(location)));
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
		final GameActions clientActions = new GameActions()
				.battlecries(new ArrayList<>())
				.chooseOnes(new ArrayList<>())
				.compatibility(new ArrayList<>())
				.discoveries(new ArrayList<>())
				.heroes(new ArrayList<>())
				.physicalAttacks(new ArrayList<>())
				.spells(new ArrayList<>())
				.summons(new ArrayList<>())
				.weapons(new ArrayList<>());

		// Get the minions' indices
		Map<Integer, Integer> minionsOrWeapons = workingContext.getEntities()
				.filter(e -> e.getEntityType() == EntityType.MINION || e.getEntityType() == EntityType.WEAPON)
				.collect(Collectors.toMap(net.demilich.metastone.game.entities.Entity::getId, e -> e.getEntityLocation().getIndex()));

		// Battlecries
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.BATTLECRY)
				.map(ga -> (BattlecryAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> {
					SpellAction spellAction = new SpellAction()
							.sourceId(kv.getKey())
							.targetKeyToActions(new ArrayList<>());

					// Targetable battlecry
					kv.getValue().stream()
							.map(t -> new TargetActionPair()
									.action(t.getId())
									.target(t.getTargetReference().getId()))
							.forEach(spellAction::addTargetKeyToActionsItem);

					return spellAction;
				})
				.forEach(clientActions::addBattlecriesItem);

		// Spells
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SPELL
						&& !(ga instanceof PlayChooseOneCardAction))
				.map(ga -> (PlaySpellCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceCardEntityId().getId()))
				.entrySet()
				.stream()
				.map(kv -> {
					final Integer sourceCardId = kv.getKey();
					final List<PlaySpellCardAction> spellCardActions = kv.getValue();
					return getSpellAction(sourceCardId, spellCardActions);
				})
				.forEach(clientActions::addSpellsItem);


		// Choose one spells
		final int[] chooseOneVirtualEntitiesId = {8000};
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SPELL
						&& ga instanceof PlayChooseOneCardAction)
				.map(ga -> (PlayChooseOneCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> buildChooseOneOptions(workingContext, playerId, chooseOneVirtualEntitiesId, kv.getKey(), kv.getValue(), ChooseOneOptions::addSpellsItem))
				.forEach(clientActions.getChooseOnes()::add);

		// Next, choose one summons
		// Choose one summons are actually SUMMON cards with different group indices from the same card
		// First,  non-choose-one summons
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SUMMON)
				.map(ga -> (PlayMinionCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().anyMatch(kv2 -> kv2.getChooseOneOptionIndex() != null))
				.map(kv -> {
					ChooseOneOptions summon = new ChooseOneOptions();
					summon.cardInHandId(kv.getKey());
					Card sourceCard = (Card) workingContext.resolveSingleTarget(new EntityReference(kv.getKey()));
					EntityLocation sourceCardLocation = sourceCard.getEntityLocation();

					kv.getValue().stream()
							.collect(Collectors.groupingBy(PlayMinionCardAction::getChooseOneOptionIndex))
							.forEach((key, summonActions) -> {
								int id = chooseOneVirtualEntitiesId[0];
								SummonAction summonAction = getSummonAction(workingContext, id, minionsOrWeapons, summonActions, playerId);
								// If it's a transform minion spell, use the entity representing the minion it's transforming into
								// Otherwise, use the source card entity with the description in the option
								Entity entity;
								String transformCardId = sourceCard.getTransformMinionCardId(key);
								boolean isTransform = transformCardId != null;
								if (isTransform) {
									entity = Games.getEntity(workingContext, CardCatalogue.getCardById(transformCardId), playerId);
								} else {
									entity = Games.getEntity(workingContext, sourceCard, playerId);
									String battlecryDescription = sourceCard.getBattlecryDescription(key);
									String battlecryName = sourceCard.getBattlecryName(key);
									entity.id(id)
											.name(battlecryName)
											.description(battlecryDescription);
								}

								entity.id(id).getState().playable(true)
										.location(Games.toClientLocation(sourceCardLocation));

								summon.addEntitiesItem(entity);
								summon.addSummonsItem(summonAction);
								chooseOneVirtualEntitiesId[0]++;
							});

					return summon;
				}).forEach(clientActions.getChooseOnes()::add);

		// Regular summons
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SUMMON)
				.map(ga -> (PlayMinionCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSummonAction(workingContext, kv.getKey(), minionsOrWeapons, kv.getValue(), playerId)).forEach(clientActions::addSummonsItem);

		// Heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue()))
				.forEach(clientActions::addHeroesItem);

		// Choose one heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().anyMatch(kv2 -> kv2.getChooseOneOptionIndex() != null))
				.map(kv -> {
					ChooseOneOptions hero = new ChooseOneOptions();
					hero.cardInHandId(kv.getKey());
					Card sourceCard = (Card) workingContext.resolveSingleTarget(new EntityReference(kv.getKey()));
					EntityLocation sourceCardLocation = sourceCard.getEntityLocation();

					kv.getValue().stream()
							.collect(Collectors.groupingBy(PlayHeroCardAction::getChooseOneOptionIndex))
							.forEach((key, battlecries) -> {
								int id = chooseOneVirtualEntitiesId[0];
								SpellAction spellAction = getSpellAction(id, battlecries);
								// If it's a transform minion spell, use the entity representing the minion it's transforming into
								// Otherwise, use the source card entity with the description in the option
								Entity entity = Games.getEntity(workingContext, sourceCard, playerId);
								String battlecryDescription = sourceCard.getBattlecryDescription(key);
								entity.id(id)
										.description(battlecryDescription)
										.getState().playable(true)
										.location(Games.toClientLocation(sourceCardLocation));

								hero.addEntitiesItem(entity);
								hero.addHeroesItem(spellAction);
								chooseOneVirtualEntitiesId[0]++;
							});

					return hero;
				}).forEach(clientActions.getChooseOnes()::add);

		// Physical attacks
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.PHYSICAL_ATTACK)
				.map(ga -> (PhysicalAttackAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getAttackerReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> new GameActionsPhysicalAttacks()
						.sourceId(kv.getKey())
						.defenders(kv.getValue().stream().map(ga ->
								new TargetActionPair().target(ga.getTargetReference().getId())
										.action(ga.getId())
						).collect(toList())))
				.forEach(clientActions::addPhysicalAttacksItem);

		// Hero powers
		Optional<SpellAction> heroPowerSpell = actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.filter(ga -> ga.getChooseOneOptionIndex() == null)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue())).findFirst();

		heroPowerSpell.ifPresent(clientActions::heroPower);

		// Choose one hero powers
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.filter(ga -> ga.getChooseOneOptionIndex() != null)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> buildChooseOneOptions(workingContext, playerId, chooseOneVirtualEntitiesId, kv.getKey(), kv.getValue(), ChooseOneOptions::addHeroPowersItem))
				.forEach(clientActions.getChooseOnes()::add);

		// Weapons
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.EQUIP_WEAPON)
				.map(ga -> (PlayWeaponCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getEntityReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> getSummonAction(workingContext, kv.getKey(), minionsOrWeapons, kv.getValue(), playerId))
				.forEach(clientActions::addWeaponsItem);

		// discovers
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.DISCOVER)
				.map(ga -> (DiscoverAction) ga)
				.map(da -> new GameActionsDiscoveries()
						.action(da.getId())
						.cardId(da.getCard().getId()))
				.forEach(clientActions::addDiscoveriesItem);


		// End Turn
		actions
				.stream()
				.filter(ga -> ga.getActionType() == ActionType.END_TURN)
				.map(ga -> (EndTurnAction) ga)
				.findFirst()
				.ifPresent(endTurnAction1 -> clientActions.endTurn(endTurnAction1.getId()));

		// Fix choose ones
		clientActions.getChooseOnes().forEach(chooseOneOptions -> {
			if (chooseOneOptions.getHeroes() == null) {
				chooseOneOptions.setHeroes(Collections.emptyList());
			}
			if (chooseOneOptions.getEntities() == null) {
				chooseOneOptions.setEntities(Collections.emptyList());
			}
			if (chooseOneOptions.getHeroPowers() == null) {
				chooseOneOptions.setHeroPowers(Collections.emptyList());
			}
			if (chooseOneOptions.getSpells() == null) {
				chooseOneOptions.setSpells(Collections.emptyList());
			}
			if (chooseOneOptions.getSummons() == null) {
				chooseOneOptions.setSummons(Collections.emptyList());
			}
		});

		// Add all the action indices for compatibility purposes
		clientActions.compatibility(actions.stream()
				.map(GameAction::getId)
				.collect(toList()));

		return clientActions;
	}

	/**
	 * Builds choose one options from a choice card, incrementing the {@code chooseOneVirtualEntitiesId} for every virtual
	 * entity it has added using {@code adder}.
	 *
	 * @param workingContext             A {@link GameContext} to query for state.
	 * @param playerId                   The point of view whom we should build the choices from.
	 * @param chooseOneVirtualEntitiesId A single-element array whose item is used to "out" the incremented entities ID
	 *                                   (i.e., {@code chooseOneVirtualEntitiesId[0]++})
	 * @param sourceId                   The source card's entity ID.
	 * @param choices                    The list of {@link PlayCardAction} choices.
	 * @param adder                      A method that accepts a {@link SpellAction} to return to the user.
	 * @param <T>                        The particular action type that supports the interface {@link HasChoiceCard}
	 * @return The choose one options.
	 */
	static <T extends PlayCardAction & HasChoiceCard> ChooseOneOptions buildChooseOneOptions(GameContext workingContext, int playerId, int[] chooseOneVirtualEntitiesId, int sourceId, List<T> choices, BiConsumer<ChooseOneOptions, SpellAction> adder) {
		ChooseOneOptions spell = new ChooseOneOptions();
		EntityLocation sourceCardLocation = ((Card) workingContext.resolveSingleTarget(choices.get(0).getEntityReference())).getEntityLocation();
		spell.cardInHandId(sourceId);

		Map<String, List<T>> intermediate = choices.stream()
				// Solves LambdaConversionException
				.collect(Collectors.groupingBy(s -> s.getChoiceCardId()));


		for (String cardId : intermediate.keySet()) {
			List<T> choiceActions = intermediate.get(cardId);

			Entity entity = Games.getEntity(workingContext, CardCatalogue.getCardById(cardId), playerId);
			int id = chooseOneVirtualEntitiesId[0];

			// Use the source card location
			entity.id(id)
					.getState().playable(true)
					.location(Games.toClientLocation(sourceCardLocation));
			SpellAction choiceSpell = getSpellAction(id, choiceActions);

			spell.addEntitiesItem(entity);
			adder.accept(spell, choiceSpell);

			chooseOneVirtualEntitiesId[0]++;
		}

		return spell;
	}

	static SummonAction getSummonAction(GameContext workingContext, Integer sourceCardId, Map<Integer, Integer> minionEntityIdToLocation, List<? extends PlayCardAction> summonActions, int playerId) {
		SummonAction summonAction = new SummonAction()
				.sourceId(sourceCardId)
				.indexToActions(summonActions.stream()
						.filter(a -> a.getTargetReference() != null)
						.map(a -> new SummonActionIndexToActions()
								.action(a.getId())
								.index(minionEntityIdToLocation.get(a.getTargetReference().getId()))).collect(toList()));

		// Add the null targeted action, if it exists
		Optional<? extends PlayCardAction> nullPlay = summonActions.stream()
				.filter(a -> a.getTargetReference() == null).findFirst();
		if (nullPlay.isPresent()) {
			GameAction a = nullPlay.get();
			summonAction.addIndexToActionsItem(
					new SummonActionIndexToActions()
							.action(a.getId())
							.index(workingContext.getPlayer(playerId).getMinions().size()));
		}
		return summonAction;
	}

	@Suspendable
	static SpellAction getSpellAction(Integer sourceCardId, List<? extends PlayCardAction> playCardActions) {
		SpellAction spellAction = new SpellAction()
				.sourceId(sourceCardId)
				.targetKeyToActions(new ArrayList<>());

		// Targetable spell
		if (playCardActions.size() == 1
				&& (playCardActions.get(0).getTargetReference() == null
				|| playCardActions.get(0).getTargetReference().isTargetGroup())) {
			spellAction.action(playCardActions.get(0).getId());
		} else {
			// Add all the valid targets
			playCardActions.stream()
					.map(t -> new TargetActionPair()
							.action(t.getId())
							.target(t.getTargetReference().getId()))
					.forEach(spellAction::addTargetKeyToActionsItem);
		}

		return spellAction;
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
		final com.hiddenswitch.spellsource.client.models.GameEvent clientEvent = new com.hiddenswitch.spellsource.client.models.GameEvent();

		clientEvent.eventType(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.valueOf(event.getEventType().toString()));

		GameContext workingContext = event.getGameContext().clone();
		// Handle the event types here.
		if (event instanceof net.demilich.metastone.game.events.PhysicalAttackEvent) {
			final net.demilich.metastone.game.events.PhysicalAttackEvent physicalAttackEvent
					= (net.demilich.metastone.game.events.PhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt, playerId);
			clientEvent.physicalAttack(physicalAttack);
		} else if (event instanceof DiscardEvent) {
			// Handles both discard and mill events
			final DiscardEvent discardEvent = (DiscardEvent) event;
			// You always see which cards get discarded
			final CardEvent cardEvent = new CardEvent()
					.card(getEntity(workingContext, discardEvent.getCard(), playerId));
			if (discardEvent.getEventType() == GameEventType.DISCARD) {
				clientEvent.discard(cardEvent);
			} else if (discardEvent.getEventType() == GameEventType.MILL) {
				clientEvent.mill(cardEvent);
			}
		} else if (event instanceof AfterPhysicalAttackEvent) {
			final AfterPhysicalAttackEvent physicalAttackEvent = (AfterPhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt, playerId);
			clientEvent.afterPhysicalAttack(physicalAttack);
		} else if (event instanceof DrawCardEvent) {
			final DrawCardEvent drawCardEvent = (DrawCardEvent) event;
			final Card card = drawCardEvent.getCard();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, playerId);
			// You never see which cards are drawn by your opponent when they go
			if (card.getOwner() != playerId) {
				entity = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
			}
			clientEvent.drawCard(new CardEvent()
					.card(entity));
		} else if (event instanceof KillEvent) {
			final KillEvent killEvent = (KillEvent) event;
			final net.demilich.metastone.game.entities.Entity victim = killEvent.getVictim();
			final com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, victim, playerId);

			clientEvent.kill(new GameEventKill()
					.victim(entity));
		} else if (event instanceof CardPlayedEvent
				|| event instanceof CardRevealedEvent) {
			final HasCard cardPlayedEvent = (HasCard) event;
			final Card card = cardPlayedEvent.getCard();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, playerId);
			if (card.getCardType() == CardType.SPELL
					&& card.isSecret()
					&& card.getOwner() != playerId
					&& event instanceof CardPlayedEvent) {
				entity = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
			}

			clientEvent.cardPlayed(new CardEvent()
					.showLocal(event instanceof CardRevealedEvent)
					.card(entity));
		} else if (event instanceof HeroPowerUsedEvent) {
			final HeroPowerUsedEvent heroPowerUsedEvent = (HeroPowerUsedEvent) event;
			final Card card = heroPowerUsedEvent.getHeroPower();
			clientEvent.heroPowerUsed(new GameEventHeroPowerUsed()
					.heroPower(getEntity(workingContext, card, playerId)));
		} else if (event instanceof SummonEvent) {
			final SummonEvent summonEvent = (SummonEvent) event;

			clientEvent.summon(new GameEventBeforeSummon()
					.minion(getEntity(workingContext, summonEvent.getMinion(), playerId))
					.source(getEntity(workingContext, summonEvent.getSource(), playerId)));
		} else if (event instanceof DamageEvent) {
			final DamageEvent damageEvent = (DamageEvent) event;
			clientEvent.damage(new GameEventDamage()
					.damage(damageEvent.getDamage())
					.source(getEntity(workingContext, damageEvent.getSource(), playerId))
					.victim(getEntity(workingContext, damageEvent.getVictim(), playerId))
					.damageType(DamageTypeEnum.fromValue(damageEvent.getDamageType().name())));
		} else if (event instanceof AfterSpellCastedEvent) {
			final AfterSpellCastedEvent afterSpellCastedEvent = (AfterSpellCastedEvent) event;
			final Card card = afterSpellCastedEvent.getCard();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, playerId);
			if (card.getCardType() == CardType.SPELL
					&& card.isSecret()
					&& card.getOwner() != playerId) {
				entity = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
			}

			clientEvent.afterSpellCasted(new GameEventAfterSpellCasted()
					.sourceCard(entity)
					.spellTarget(getEntity(workingContext, afterSpellCastedEvent.getEventTarget(), playerId)));
		} else if (event instanceof SecretRevealedEvent) {
			final SecretRevealedEvent secretRevealedEvent = (SecretRevealedEvent) event;
			clientEvent.secretRevealed(new GameEventSecretRevealed()
					.secret(getEntity(workingContext, secretRevealedEvent.getCard(), playerId)));
		} else if (event instanceof QuestSuccessfulEvent) {
			final QuestSuccessfulEvent questSuccessfulEvent = (QuestSuccessfulEvent) event;
			clientEvent.questSuccessful(new GameEventQuestSuccessful()
					.quest(getEntity(workingContext, questSuccessfulEvent.getCard(), playerId)));
		} else if (event instanceof JoustEvent) {
			final JoustEvent joustEvent = (JoustEvent) event;
			clientEvent.joust(new GameEventJoust()
					.ownCard(getEntity(workingContext, joustEvent.getOwnCard(), playerId))
					.opponentCard(getEntity(workingContext, joustEvent.getOpponentCard(), playerId))
					.won(joustEvent.isWon()));
		} else if (event instanceof FatigueEvent) {
			final FatigueEvent fatigueEvent = (FatigueEvent) event;
			clientEvent.fatigue(new GameEventFatigue()
					.damage(fatigueEvent.getValue())
					.playerId(fatigueEvent.getTargetPlayerId()));
		}

		return clientEvent;
	}

	static com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent getPhysicalAttack(GameContext workingContext, Actor attacker, Actor defender, int damageDealt, int playerId) {
		return new com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent()
				.attacker(getEntity(workingContext, attacker, playerId))
				.defender(getEntity(workingContext, defender, playerId))
				.damageDealt(damageDealt);
	}

	@Suspendable
	/**
	 * Retrieves the current connections by Game ID
	 *
	 * @param vertx The {@link Vertx} that the verticle should use to connect for {@link SharedData}
	 * @return A {@link io.vertx.core.shareddata.LocalMap} or a {@link SuspendableAsyncMap},
	 * depending on whether or not the underlying {@link SharedData} is operating on a {@link Vertx#isClustered()}
	 * instance.
	 */
	static SuspendableMap<GameId, CreateGameSessionResponse> getConnections() throws SuspendExecution {
		return SuspendableMap.getOrCreate("Games::connections");
	}

	static SuspendableMap<UserId, GameId> getGames() throws SuspendExecution {
		return SuspendableMap.getOrCreate("Games::players");
	}

	/**
	 * Creates a game session on this instance.
	 *
	 * @param request Information needed to start a game.
	 * @return Information for the users to connect to the game.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Gets the current game state of a requested game ID. In the future, this method should punt the request to the next
	 * Games service instance if it can't find the given session.
	 *
	 * @param request The game ID to describe.
	 * @return The game state of the requested game.
	 */
	DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request);

	/**
	 * Possibly prematurely ends the game session. Typically this is done due to timeouts or some external action that
	 * would concede a game (like a ban or profanity). This will send the correct game over notifications to the bots/
	 * players who are connected to this game.
	 *
	 * @param request The game to end.
	 * @return Information about ending the specified game session.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution;

	/**
	 * Updates an entity specified inside the game with specific attributes. Currently unsupported. This allows real-time
	 * manipulation of a game in progress. This call should punt the request to the next instance in the cluster if it
	 * does not have the specified game.
	 *
	 * @param request Information about the game and the updates to the entity this service should do.
	 * @return Information about the entity update.
	 * @throws UnsupportedOperationException
	 */
	@Suspendable
	UpdateEntityResponse updateEntity(UpdateEntityRequest request) throws UnsupportedOperationException;

	/**
	 * Performs a game action in the specified game.
	 *
	 * @param request A request containing the ID of the game and action to perform.
	 * @return A response indicating the new game state.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	@Suspendable
	PerformGameActionResponse performGameAction(PerformGameActionRequest request) throws InterruptedException, SuspendExecution;

	/**
	 * Concedes the specified game session. Unlike ending a game session prematurely, a concession may trigger some
	 * additional notifications and scoring consequences.
	 *
	 * @param request The player and game conceding.
	 * @return Any consequences of the concession.
	 * @throws InterruptedException
	 * @throws SuspendExecution
	 */
	@Suspendable
	ConcedeGameSessionResponse concedeGameSession(ConcedeGameSessionRequest request) throws InterruptedException, SuspendExecution;

	/**
	 * Gets a complete view of the game for the specified user, respecting security (i.e., information about the user's
	 * opponent's deck, hand and secrets is not leaked).
	 *
	 * @param gameId The game to get client state for.
	 * @param userId The user ID whose point of view this state should be generated for.
	 * @return A client view game state.
	 */
	default GameState getClientGameState(String gameId, String userId) {
		DescribeGameSessionResponse gameSession = describeGameSession(DescribeGameSessionRequest.create(gameId));
		final com.hiddenswitch.spellsource.common.GameState state = gameSession.getState();
		GameContext workingContext = new GameContext();
		workingContext.setGameState(state);
		final Player local;
		final Player opponent;
		if (state.player1.getUserId().equals(userId)) {
			local = state.player1;
			opponent = state.player2;
		} else {
			local = state.player2;
			opponent = state.player1;
		}

		return getGameState(workingContext, local, opponent);
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
	static GameState getGameState(GameContext workingContext, final Player local, final Player opponent) {
		List<com.hiddenswitch.spellsource.client.models.Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible
		int localPlayerId = local.getId();

		List<com.hiddenswitch.spellsource.client.models.Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			final com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, localPlayerId);
			localHand.add(entity);
		}

		// Add complete information for the local hand
		entities.addAll(localHand);

		for (EntityZone<Minion> battlefield : Arrays.asList(local.getMinions(), opponent.getMinions())) {
			List<com.hiddenswitch.spellsource.client.models.Entity> minions = new ArrayList<>();
			for (Minion minion : battlefield) {
				final com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, minion, localPlayerId);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
		}

		List<com.hiddenswitch.spellsource.client.models.Entity> localSecrets = new ArrayList<>();
		// Add complete information for the local secrets
		for (Secret secret : local.getSecrets()) {
			final com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, secret, localPlayerId);
			localSecrets.add(entity);
		}

		entities.addAll(localSecrets);

		// Add limited information for opposing secrets
		List<com.hiddenswitch.spellsource.client.models.Entity> opposingSecrets = new ArrayList<>();
		for (Secret secret : opponent.getSecrets()) {
			final com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
					.id(secret.getId())
					.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.SECRET)
					.state(new EntityState()
							.owner(secret.getOwner())
							.heroClass(secret.getSourceCard().getHeroClass().toString())
							.location(Games.toClientLocation(secret.getEntityLocation())));
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
		for (final Player player : Arrays.asList(local, opponent)) {
			com.hiddenswitch.spellsource.client.models.Entity playerEntity = new com.hiddenswitch.spellsource.client.models.Entity()
					.id(player.getId())
					.name(player.getName())
					.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.PLAYER)
					.state(new EntityState()
							.owner(player.getId())
							.lockedMana(player.getLockedMana())
							.maxMana(player.getMaxMana())
							.mana(player.getMana())
							.location(Games.toClientLocation(player.getEntityLocation()))
							.gameStarted(player.hasAttribute(Attribute.GAME_STARTED)));
			playerEntities.add(playerEntity);
			// The heroes may have wound up in the graveyard
			final com.hiddenswitch.spellsource.client.models.Entity heroEntity = getEntity(workingContext, player.getHero(), localPlayerId);

			if (heroEntity == null) {
				continue;
			}

			// Include the player's mana, locked mana and max mana in the hero entity for convenience
			heroEntity.getState()
					.mana(player.getMana())
					.maxMana(player.getMaxMana())
					.lockedMana(player.getLockedMana());
			playerEntities.add(heroEntity);
			if (player.getHero().getHeroPower() != null) {
				final com.hiddenswitch.spellsource.client.models.Entity heroPowerEntity = getEntity(workingContext, player.getHero().getHeroPower(), localPlayerId);
				playerEntities.add(heroPowerEntity);
			}
			if (player.getHero().getWeapon() != null) {
				final com.hiddenswitch.spellsource.client.models.Entity weaponEntity = getEntity(workingContext, player.getHero().getWeapon(), localPlayerId);
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
		final List<Entity> graveyardHeroes = Stream.of(local.getGraveyard().stream(), opponent.getGraveyard().stream(), local.getRemovedFromPlay().stream(), opponent.getRemovedFromPlay().stream()).flatMap(e -> e)
				.filter(e -> e.getEntityType() == EntityType.HERO)
				.map(h -> {
					final Entity e = getEntity(workingContext, h, localPlayerId);
					Player owner = h.getOwner() == local.getId() ? local : opponent;
					e.getState()
							.mana(owner.getMana())
							.maxMana(owner.getMaxMana())
							.lockedMana(owner.getLockedMana());
					return e;
				})
				// Don't include heroes that have already been added
				.filter(e -> playerEntities.stream().noneMatch(v -> v.getId().equals(e.getId())))
				.collect(toList());
		entities.addAll(graveyardHeroes);

		// Any missing entities will get a stand-in entry
		Set<Integer> visibleEntityIds = entities.stream().map(com.hiddenswitch.spellsource.client.models.Entity::getId).collect(Collectors.toSet());
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId())).map(e -> new com.hiddenswitch.spellsource.client.models.Entity()
				.id(e.getId())
				.cardId("hidden")
				.state(new EntityState()
						.location(toClientLocation(e.getEntityLocation())))
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.valueOf(e.getEntityType().toString()))).collect(toList()));

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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(final GameContext workingContext, final net.demilich.metastone.game.entities.Entity entity, int localPlayerId) {
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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(final GameContext workingContext, final Actor actor, int localPlayerId) {
		if (actor == null) {
			return null;
		}

		// For the purposes of determining whether or not the game is over, we will calculate the match result once
		if (workingContext.getStatus() == null) {
			workingContext.updateAndGetGameOver();
		}

		final Card card = actor.getSourceCard();
		final EntityState entityState = new EntityState();
		final com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
				.description(actor.getDescription())
				.name(actor.getName())
				.id(actor.getId())
				.cardId(card.getCardId());

		if (actor instanceof Minion) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.MINION);
			entityState.boardPosition(actor.getEntityLocation().getIndex());
		} else if (actor instanceof Hero) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.HERO);
			entityState.armor(actor.getArmor());
		} else if (actor instanceof Weapon) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.WEAPON);
		}

		entityState.owner(actor.getOwner());
		entityState.location(Games.toClientLocation(actor.getEntityLocation()));
		entityState.manaCost(card.getBaseManaCost());
		entityState.heroClass(card.getHeroClass().toString());
		entityState.cardSet(Objects.toString(card.getCardSet()));
		entityState.rarity(card.getRarity() != null ? card.getRarity().getClientRarity() : null);
		entityState.baseManaCost(card.getBaseManaCost());
		entityState.silenced(actor.hasAttribute(Attribute.SILENCED));
		entityState.deathrattles(actor.getDeathrattles() != null);
		final boolean playable = actor.getOwner() == workingContext.getActivePlayerId()
				&& actor.getOwner() == localPlayerId
				&& workingContext.getStatus() == GameStatus.RUNNING
				&& actor.canAttackThisTurn();
		entityState.playable(playable);
		entityState.attack(actor.getAttack());
		entityState.baseAttack(actor.getBaseAttack());
		entityState.baseHp(actor.getBaseHp());
		entityState.hp(actor.getHp());
		entityState.maxHp(actor.getMaxHp());
		entityState.heroClass(actor.getHeroClass().toString());
		entityState.underAura(actor.hasAttribute(Attribute.AURA_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.AURA_HP_BONUS)
				|| actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
				|| actor.hasAttribute(Attribute.AURA_TAUNT)
				|| actor.hasAttribute(Attribute.HP_BONUS)
				|| actor.hasAttribute(Attribute.ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
		entityState.frozen(actor.hasAttribute(Attribute.FROZEN));
		entityState.charge(actor.hasAttribute(Attribute.CHARGE) || actor.hasAttribute(Attribute.AURA_CHARGE) || actor.hasAttribute(Attribute.RUSH) || actor.hasAttribute(Attribute.AURA_RUSH));
		entityState.immune(actor.hasAttribute(Attribute.IMMUNE) || actor.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
		entityState.stealth(actor.hasAttribute(Attribute.STEALTH) || actor.hasAttribute(Attribute.AURA_STEALTH));
		entityState.taunt(actor.hasAttribute(Attribute.TAUNT) || actor.hasAttribute(Attribute.AURA_TAUNT));
		entityState.divineShield(actor.hasAttribute(Attribute.DIVINE_SHIELD));
		entityState.deflect(actor.hasAttribute(Attribute.DEFLECT));
		entityState.enraged(actor.hasAttribute(Attribute.ENRAGED));
		entityState.destroyed(actor.hasAttribute(Attribute.DESTROYED));
		entityState.cannotAttack(actor.hasAttribute(Attribute.CANNOT_ATTACK) || actor.hasAttribute(Attribute.AURA_CANNOT_ATTACK));
		entityState.spellDamage(actor.getAttributeValue(Attribute.SPELL_DAMAGE));
		entityState.windfury(actor.hasAttribute(Attribute.WINDFURY) || actor.hasAttribute(Attribute.AURA_WINDFURY));
		entityState.lifesteal(actor.hasAttribute(Attribute.LIFESTEAL) || actor.hasAttribute(Attribute.LIFESTEAL));
		entityState.poisonous(actor.hasAttribute(Attribute.POISONOUS) || actor.hasAttribute(Attribute.AURA_POISONOUS));
		entityState.summoningSickness(actor.hasAttribute(Attribute.SUMMONING_SICKNESS));
		entityState.untargetableBySpells(actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS) || actor.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS));
		entityState.permanent(actor.hasAttribute(Attribute.PERMANENT));
		entityState.rush(actor.hasAttribute(Attribute.RUSH) || actor.hasAttribute(Attribute.AURA_RUSH));
		entityState.tribe(actor.getRace() != null ? actor.getRace().name() : null);
		final List<Trigger> triggers = workingContext.getTriggerManager().getTriggersAssociatedWith(actor.getReference());
		entityState.hostsTrigger(triggers.size() > 0);
		entity.state(entityState);
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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(final GameContext workingContext, final Enchantment enchantment, int localPlayerId) {
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
		Entity.EntityTypeEnum entityType = Entity.EntityTypeEnum.SECRET;
		if (enchantment instanceof Quest) {
			entityType = Entity.EntityTypeEnum.QUEST;
		}

		entity.getState()
				.fires(enchantment.getFires())
				.countUntilCast(enchantment.getCountUntilCast());

		entity.id(enchantment.getId())
				.entityType(entityType)
				.getState()
				.location(Games.toClientLocation(enchantment.getEntityLocation()))
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
	static com.hiddenswitch.spellsource.client.models.Entity getEntity(final GameContext workingContext, final Card card, int localPlayerId) {
		if (card == null) {
			return null;
		}

		final com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
				.description(card.getDescription())
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.CARD)
				.name(card.getName())
				.description(card.getDescription())
				.id(card.getId())
				.cardId(card.getCardId());
		final EntityState entityState = new EntityState();
		int owner = card.getOwner();
		Player owningPlayer;
		if (owner != -1) {
			if (card.getZone() == Zones.HAND
					|| card.getZone() == Zones.DECK
					|| card.getZone() == Zones.SET_ASIDE_ZONE
					|| card.getZone() == Zones.HERO_POWER
					&& owner == localPlayerId) {
				final boolean playable = workingContext.getLogic().canPlayCard(owner, card.getReference())
						&& card.getOwner() == workingContext.getActivePlayerId()
						&& localPlayerId == card.getOwner();
				entityState.playable(playable);
				entityState.manaCost(workingContext.getLogic().getModifiedManaCost(workingContext.getPlayer(owner), card));
			} else {
				entityState.playable(false);
				entityState.manaCost(card.getBaseManaCost());
			}
			owningPlayer = workingContext.getPlayer(card.getOwner());
		} else {
			entityState.playable(false);
			entityState.manaCost(card.getBaseManaCost());
			owningPlayer = Player.empty();
		}

		entityState.owner(card.getOwner());
		entityState.cardSet(Objects.toString(card.getCardSet()));
		entityState.rarity(card.getRarity() != null ? card.getRarity().getClientRarity() : null);
		entityState.location(Games.toClientLocation(card.getEntityLocation()));
		entityState.baseManaCost(card.getBaseManaCost());
		entityState.uncensored(card.hasAttribute(Attribute.UNCENSORED));
		entityState.battlecry(card.hasAttribute(Attribute.BATTLECRY));
		entityState.deathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
		entityState.permanent(card.hasAttribute(Attribute.PERMANENT));
		entityState.collectible(card.isCollectible());
		// TODO: A little too underperformant so we're going to skip this
		// entityState.conditionMet(workingContext.getLogic().conditionMet(localPlayerId, card));
		HeroClass heroClass = card.getHeroClass();

		// Handles tri-class cards correctly
		if (heroClass == null) {
			heroClass = HeroClass.ANY;
		}

		entityState.heroClass(heroClass.toString());
		entityState.cardType(EntityState.CardTypeEnum.valueOf(card.getCardType().toString()));
		final boolean hostsTrigger = workingContext.getTriggerManager().getTriggersAssociatedWith(card.getReference()).size() > 0;
		// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
		switch (card.getCardType()) {
			case HERO:
				// Retrieve the weapon attack
				Card weapon = card.getWeapon();
				if (weapon != null) {
					entityState.attack(weapon.getBaseDamage());
				}
				entityState.armor(card.getArmor());
				break;
			case MINION:
				entityState.attack(card.getAttack() + card.getBonusAttack());
				entityState.baseAttack(card.getBaseAttack());
				entityState.baseManaCost(card.getBaseManaCost());
				entityState.hp(card.getHp());
				entityState.baseHp(card.getBaseHp());
				entityState.maxHp(card.getBaseHp() + card.getBonusHp());
				entityState.underAura(card.getBonusAttack() > 0
						|| card.getBonusAttack() > 0
						|| hostsTrigger);
				entityState.tribe(card.getRace() != null ? card.getRace().name() : null);
				break;
			case WEAPON:
				entityState.durability(card.getDurability());
				entityState.hp(card.getDurability());
				entityState.maxHp(card.getBaseDurability() + card.getBonusDurability());
				entityState.attack(card.getDamage() + card.getBonusDamage());
				entityState.underAura(card.getBonusDamage() > 0
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

				if (card.getZone() == Zones.HAND
						&& DamageSpell.class.isAssignableFrom(spell.getDescClass())
						&& owningPlayer != null) {

					Minion oneOne = CardCatalogue.getCardById("minion_snowflipper_penguin").summon();
					oneOne.setId(65535);
					damage = DamageSpell.getDamage(workingContext, owningPlayer, card.getSpell(), card, oneOne);
					spellpowerDamage = workingContext.getLogic().applySpellpower(owningPlayer, card, damage);
				}
				entityState.underAura(spellpowerDamage > damage
						|| hostsTrigger);
				entityState.spellDamage(spellpowerDamage);
				break;
			case CHOOSE_ONE:
				// TODO: Handle choose one cards
				break;
		}
		entity.state(entityState);
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
				.zone(com.hiddenswitch.spellsource.client.models.EntityLocation.ZoneEnum.valueOf(location.getZone().toString()))
				.index(location.getIndex())
				.player(location.getPlayer());
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
	 * Creates a web socket handler to route game traffic (actions, game states, etc.) between the HTTP/WS client this
	 * handler will create and the appropriate event bus address for game traffic.
	 *
	 * @return A suspendable handler.
	 */
	static Handler<RoutingContext> createWebSocketHandler() {
		return Sync.suspendableHandler(context -> {
			final String userId = context.user().principal().getString("_id");
			final Vertx vertx = context.vertx();
			final EventBus bus = vertx.eventBus();
			try {
				Lock lock = awaitResult(h -> vertx.sharedData().getLockWithTimeout("pipes-userId-" + userId, 200L, h));
				LOGGER.debug("createWebSocketHandler: Creating WebSocket to EventBus mapping for userId {}", userId);
				final ServerWebSocket socket;
				final HttpServerRequest request = context.request();
				try {
					socket = request.upgrade();
				} catch (IllegalStateException ex) {
					LOGGER.error("createWebSocketHandler: Failed to upgrade with error: {}. Request={}", new ToStringBuilder(request)
							.append("headers", request.headers().entries())
							.append("uri", request.uri())
							.append("userId", userId).toString());
					throw ex;
				}
				final MessageConsumer<Buffer> consumer = bus.consumer(EventBusWriter.WRITER_ADDRESS_PREFIX + userId);
				final MessageProducer<Buffer> publisher = bus.publisher(ClusteredGamesImpl.READER_ADDRESS_PREFIX + userId);
				final Pump pump1 = new SuspendablePump<>(socket, publisher, Integer.MAX_VALUE).start();
				final Pump pump2 = new SuspendablePump<>(consumer.bodyStream(), socket, Integer.MAX_VALUE).start();

				socket.closeHandler(fiberHandler(disconnected -> {
					try {
						// Include a reference in this lambda to ensure the pump lasts
						pump2.numberPumped();
						publisher.close();
						consumer.unregister();
						pump1.stop();
					} catch (Throwable throwable) {
						LOGGER.warn("createWebSocketHandler socket closeHandler: Failed to clean up resources from a user {} socket due to an exception {}", userId, throwable);
					} finally {
						lock.release();
					}
				}));
			} catch (VertxException timeout) {
				LOGGER.debug("createWebSocketHandler: Lock was not obtained for userId {}, user probably has another mapping already", userId);
				context.response().end();
			}
		});
	}
}
