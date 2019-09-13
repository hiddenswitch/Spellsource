package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.ClusteredGames;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Rpc;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.events.PhysicalAttackEvent;
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
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.CARD)
				.description("A secret! This card will be revealed when a certain action occurs.")
				.name("Secret")
				.id(id)
				.owner(owner)
				.cardType(Entity.CardTypeEnum.SPELL)
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
		GameActions clientActions = new GameActions()
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
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> {
					Integer sourceCardId = kv.getKey();
					List<PlaySpellCardAction> spellCardActions = kv.getValue();
					return getSpellAction(sourceCardId, spellCardActions);
				})
				.forEach(clientActions::addSpellsItem);


		// Choose one spells
		int[] chooseOneVirtualEntitiesId = {8000};
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SPELL
						&& ga instanceof PlayChooseOneCardAction)
				.map(ga -> (PlayChooseOneCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
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
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
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

								entity.id(id).playable(true)
										.l(Games.toClientLocation(sourceCardLocation));

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
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSummonAction(workingContext, kv.getKey(), minionsOrWeapons, kv.getValue(), playerId)).forEach(clientActions::addSummonsItem);

		// Heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue()))
				.forEach(clientActions::addHeroesItem);

		// Choose one heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
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
										.description(battlecryDescription).playable(true)
										.l(Games.toClientLocation(sourceCardLocation));

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
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue())).findFirst();

		heroPowerSpell.ifPresent(clientActions::heroPower);

		// Choose one hero powers
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.filter(ga -> ga.getChooseOneOptionIndex() != null)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
				.entrySet()
				.stream()
				.map(kv -> buildChooseOneOptions(workingContext, playerId, chooseOneVirtualEntitiesId, kv.getKey(), kv.getValue(), ChooseOneOptions::addHeroPowersItem))
				.forEach(clientActions.getChooseOnes()::add);

		// Weapons
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.EQUIP_WEAPON)
				.map(ga -> (PlayWeaponCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSourceReference().getId()))
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
		EntityLocation sourceCardLocation = workingContext.resolveSingleTarget(choices.get(0).getSourceReference()).getEntityLocation();
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
					.playable(true)
					.l(Games.toClientLocation(sourceCardLocation));
			SpellAction choiceSpell = getSpellAction(id, choiceActions);

			spell.addEntitiesItem(entity);
			adder.accept(spell, choiceSpell);

			chooseOneVirtualEntitiesId[0]++;
		}

		return spell;
	}

	/**
	 * Builds a summon action from a minion card.
	 *
	 * @param workingContext
	 * @param sourceCardId
	 * @param minionEntityIdToLocation
	 * @param summonActions
	 * @param playerId
	 * @return
	 */
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

	/**
	 * Builds a spell action from a spell card or a hero power card. Any play card action that accepts targets can work
	 * for this function.
	 *
	 * @param sourceCardId
	 * @param playCardActions
	 * @return
	 */
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
		com.hiddenswitch.spellsource.client.models.GameEvent clientEvent = new com.hiddenswitch.spellsource.client.models.GameEvent();

		clientEvent.eventType(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.valueOf(event.getEventType().toString()));

		GameContext workingContext = event.getGameContext().clone();
		// Handle the event types here.
		if (event instanceof PhysicalAttackEvent) {
			net.demilich.metastone.game.events.PhysicalAttackEvent physicalAttackEvent
					= (net.demilich.metastone.game.events.PhysicalAttackEvent) event;
			Actor attacker = physicalAttackEvent.getAttacker();
			Actor defender = physicalAttackEvent.getDefender();
			int damageDealt = physicalAttackEvent.getDamageDealt();
			com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt, playerId);
			if (event.getEventType() == GameEventType.PHYSICAL_ATTACK) {
				clientEvent.physicalAttack(physicalAttack);
			} else if (event.getEventType() == GameEventType.AFTER_PHYSICAL_ATTACK) {
				clientEvent.afterPhysicalAttack(physicalAttack);
			}
		} else if (event instanceof DiscardEvent) {
			// Handles discard and roast events
			DiscardEvent discardEvent = (DiscardEvent) event;
			// You always see which cards get discarded
			CardEvent cardEvent = new CardEvent()
					.card(getEntity(workingContext, discardEvent.getCard(), playerId));
			if (discardEvent.getEventType() == GameEventType.DISCARD) {
				clientEvent.discard(cardEvent);
			} else if (discardEvent.getEventType() == GameEventType.ROASTED) {
				clientEvent.roasted(cardEvent);
			}
		} else if (event instanceof DrawCardEvent) {
			DrawCardEvent drawCardEvent = (DrawCardEvent) event;
			Card card = drawCardEvent.getCard();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, playerId);
			// You never see which cards are drawn by your opponent when they go
			if (card.getOwner() != playerId) {
				entity = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
			}
			clientEvent.drawCard(new CardEvent()
					.card(entity));
		} else if (event instanceof KillEvent) {
			KillEvent killEvent = (KillEvent) event;
			net.demilich.metastone.game.entities.Entity victim = killEvent.getVictim();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, victim, playerId);

			clientEvent.kill(new GameEventKill()
					.victim(entity));
		} else if (event instanceof CardPlayedEvent
				|| event instanceof CardRevealedEvent) {
			HasCard cardPlayedEvent = (HasCard) event;
			Card card = cardPlayedEvent.getCard();
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
			HeroPowerUsedEvent heroPowerUsedEvent = (HeroPowerUsedEvent) event;
			Card card = heroPowerUsedEvent.getHeroPower();
			clientEvent.heroPowerUsed(new GameEventHeroPowerUsed()
					.heroPower(getEntity(workingContext, card, playerId)));
			// Only send exactly the before summon event data
		} else if (event.getClass().equals(BeforeSummonEvent.class)) {
			SummonEvent summonEvent = (SummonEvent) event;

			clientEvent.summon(new GameEventBeforeSummon()
					.minion(getEntity(workingContext, summonEvent.getMinion(), playerId))
					.source(getEntity(workingContext, summonEvent.getSource(), playerId)));
		} else if (event instanceof DamageEvent) {
			DamageEvent damageEvent = (DamageEvent) event;
			clientEvent.damage(new GameEventDamage()
					.damage(damageEvent.getDamage())
					.source(getEntity(workingContext, damageEvent.getSource(), playerId))
					.victim(getEntity(workingContext, damageEvent.getVictim(), playerId))
					.damageType(DamageTypeEnum.fromValue(damageEvent.getDamageType().name())));
		} else if (event instanceof AfterSpellCastedEvent) {
			AfterSpellCastedEvent afterSpellCastedEvent = (AfterSpellCastedEvent) event;
			Card card = afterSpellCastedEvent.getCard();
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
			SecretRevealedEvent secretRevealedEvent = (SecretRevealedEvent) event;
			clientEvent.secretRevealed(new GameEventSecretRevealed()
					.secret(getEntity(workingContext, secretRevealedEvent.getCard(), playerId)));
		} else if (event instanceof QuestSuccessfulEvent) {
			QuestSuccessfulEvent questSuccessfulEvent = (QuestSuccessfulEvent) event;
			clientEvent.questSuccessful(new GameEventQuestSuccessful()
					.quest(getEntity(workingContext, questSuccessfulEvent.getCard(), playerId)));
		} else if (event instanceof JoustEvent) {
			JoustEvent joustEvent = (JoustEvent) event;
			clientEvent.joust(new GameEventJoust()
					.ownCard(getEntity(workingContext, joustEvent.getOwnCard(), playerId))
					.opponentCard(getEntity(workingContext, joustEvent.getOpponentCard(), playerId))
					.won(joustEvent.isWon()));
		} else if (event instanceof FatigueEvent) {
			FatigueEvent fatigueEvent = (FatigueEvent) event;
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
	 * @param vertx The {@link Vertx} that the verticle should use to connect for {@link Hazelcast}
	 * @return A map.
	 */
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
	 * Immediately ends the given game, causing both players to concede.
	 * <p>
	 * All games ended this way end in a draw.
	 *
	 * @param game
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static void endGame(GameId game) throws SuspendExecution, InterruptedException {
		try {
			String deploymentId = getConnections().get(game).deploymentId;
			Rpc.connect(Games.class).sync(deploymentId).endGameSession(new EndGameSessionRequest(game.toString()));
		} catch (NullPointerException notFound) {
			NullPointerException rethrown = new NullPointerException(String.format("The specified game %s was not found", game.toString()));
			rethrown.setStackTrace(notFound.getStackTrace());
			throw rethrown;
		}
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
	 * com.hiddenswitch.spellsource.impl.util.ServerGameContext} that was just created.
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
					.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.SECRET)
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
					.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.PLAYER)
					.owner(player.getId())
					.lockedMana(player.getLockedMana())
					.maxMana(player.getMaxMana())
					.mana(player.getMana())
					.l(Games.toClientLocation(player.getEntityLocation()))
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
			if (player.getHero().getHeroPower() != null) {
				com.hiddenswitch.spellsource.client.models.Entity heroPowerEntity = getEntity(workingContext, player.getHero().getHeroPower(), localPlayerId);
				playerEntities.add(heroPowerEntity);
			}
			if (player.getHero().getWeapon() != null) {
				com.hiddenswitch.spellsource.client.models.Entity weaponEntity = getEntity(workingContext, player.getHero().getWeapon(), localPlayerId);
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
				.filter(e -> e.getEntityType() == EntityType.HERO)
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

		// Any missing entities will get a stand-in entry
		Set<Integer> visibleEntityIds = entities.stream().map(com.hiddenswitch.spellsource.client.models.Entity::getId).collect(Collectors.toSet());
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId())).map(e -> new com.hiddenswitch.spellsource.client.models.Entity()
				.id(e.getId())
				.owner(e.getOwner())
				.l(toClientLocation(e.getEntityLocation()))
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

		Card card = actor.getSourceCard();
		com.hiddenswitch.spellsource.client.models.Entity entity = new com.hiddenswitch.spellsource.client.models.Entity()
				.description(actor.getDescription(workingContext, workingContext.getPlayer(actor.getOwner())))
				.name(actor.getName())
				.id(actor.getId())
				.cardId(card.getCardId());

		if (actor instanceof Minion) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.MINION);
			entity.boardPosition(actor.getEntityLocation().getIndex());
		} else if (actor instanceof Hero) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.HERO);
			entity.armor(actor.getArmor());
		} else if (actor instanceof Weapon) {
			entity.setEntityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.WEAPON);
		}

		entity.owner(actor.getOwner());
		entity.l(Games.toClientLocation(actor.getEntityLocation()));
		entity.manaCost(card.getBaseManaCost());
		entity.heroClass(card.getHeroClass().toString());
		entity.cardSet(Objects.toString(card.getCardSet()));
		entity.rarity(card.getRarity() != null ? card.getRarity().getClientRarity() : null);
		entity.baseManaCost(card.getBaseManaCost());
		entity.silenced(actor.hasAttribute(Attribute.SILENCED));
		entity.deathrattles(!actor.getDeathrattles().isEmpty());
		boolean playable = actor.getOwner() == workingContext.getActivePlayerId()
				&& actor.getOwner() == localPlayerId
				&& workingContext.getStatus() == GameStatus.RUNNING
				&& actor.canAttackThisTurn();
		entity.playable(playable);
		entity.attack(actor.getAttack());
		entity.baseAttack(actor.getBaseAttack());
		entity.baseHp(actor.getBaseHp());
		entity.hp(actor.getHp());
		entity.maxHp(actor.getMaxHp());
		entity.heroClass(actor.getHeroClass().toString());
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
		List<Trigger> triggers = workingContext.getTriggerManager().getTriggersAssociatedWith(actor.getReference());
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
		Entity.EntityTypeEnum entityType;
		if (enchantment instanceof Secret) {
			entityType = Entity.EntityTypeEnum.SECRET;
		} else if (enchantment instanceof Quest) {
			entityType = Entity.EntityTypeEnum.QUEST;
		} else {
			entityType = Entity.EntityTypeEnum.ENCHANTMENT;
		}

		entity
				.fires(enchantment.getFires())
				.countUntilCast(enchantment.getCountUntilCast());

		entity.id(enchantment.getId())
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
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.CARD)
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
		entity.rarity(card.getRarity() != null ? card.getRarity().getClientRarity() : null);
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
		entity.cardType(Entity.CardTypeEnum.valueOf(card.getCardType().toString()));
		boolean hostsTrigger = workingContext.getTriggerManager().getTriggersAssociatedWith(card.getReference()).size() > 0;
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
	 * 		to disconnection.
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
		for (WhereverTheyAreEnchantment e : context.getTriggerManager().getTriggers()
				.stream()
				.filter(e -> e.getOwner() == playerId && e instanceof WhereverTheyAreEnchantment)
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
