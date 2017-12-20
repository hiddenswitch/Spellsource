package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Entity;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.ClusteredGamesImpl;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.Verticle;
import net.demilich.metastone.game.events.HasCard;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.*;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * A service that starts a game session, accepts connections from players and manages the state of the game.
 */
public interface Games {
	long DEFAULT_NO_ACTIVITY_TIMEOUT = 225000L;
	String WEBSOCKET_PATH = "games";

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
		final GameActions clientActions = new GameActions();

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
							.sourceId(kv.getKey());

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
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
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
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().anyMatch(kv2 -> kv2.getChooseOneOptionIndex() != null))
				.map(kv -> {
					ChooseOneOptions summon = new ChooseOneOptions();
					summon.cardInHandId(kv.getKey());
					ChooseBattlecryMinionCard sourceCard = (ChooseBattlecryMinionCard) workingContext.resolveSingleTarget(new EntityReference(kv.getKey()));
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
									entity.id(id)
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
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSummonAction(workingContext, kv.getKey(), minionsOrWeapons, kv.getValue(), playerId)).forEach(clientActions::addSummonsItem);

		// Heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().allMatch(kv2 -> kv2.getChooseOneOptionIndex() == null))
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue()))
				.forEach(clientActions::addHeroesItem);

		// Choose one heroes
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO)
				.map(ga -> (PlayHeroCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.filter(kv -> kv.getValue().stream().anyMatch(kv2 -> kv2.getChooseOneOptionIndex() != null))
				.map(kv -> {
					ChooseOneOptions hero = new ChooseOneOptions();
					hero.cardInHandId(kv.getKey());
					ChooseBattlecryHeroCard sourceCard = (ChooseBattlecryHeroCard) workingContext.resolveSingleTarget(new EntityReference(kv.getKey()));
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
						).collect(Collectors.toList())))
				.forEach(clientActions::addPhysicalAttacksItem);

		// Hero powers
		Optional<SpellAction> heroPowerSpell = actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.filter(ga -> ga.getChooseOneOptionIndex() == null)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.map(kv -> getSpellAction(kv.getKey(), kv.getValue())).findFirst();

		heroPowerSpell.ifPresent(clientActions::heroPower);

		// Choose one hero powers
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.filter(ga -> ga.getChooseOneOptionIndex() != null)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
				.entrySet()
				.stream()
				.map(kv -> buildChooseOneOptions(workingContext, playerId, chooseOneVirtualEntitiesId, kv.getKey(), kv.getValue(), ChooseOneOptions::addHeroPowersItem))
				.forEach(clientActions.getChooseOnes()::add);

		// Weapons
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.EQUIP_WEAPON)
				.map(ga -> (PlayWeaponCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getEntityId()))
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

		// Add all the action indices for compatibility purposes
		clientActions.compatibility(actions.stream()
				.map(GameAction::getId)
				.collect(Collectors.toList()));

		return clientActions;
	}

	/**
	 * Builds choose one options from a choice card, incrementing the {@code chooseOneVirtualEntitiesId} for every
	 * virtual entity it has added using {@code adder}.
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
		EntityLocation sourceCardLocation = workingContext.resolveCardReference(choices.get(0).getCardReference()).getEntityLocation();
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
								.index(minionEntityIdToLocation.get(a.getTargetReference().getId()))).collect(Collectors.toList()));

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
				.sourceId(sourceCardId);

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
		} else if (DiscardEvent.class.isAssignableFrom(event.getClass())) {
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
					&& card instanceof SecretCard
					&& card.getOwner() != playerId) {
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
					.victim(getEntity(workingContext, damageEvent.getVictim(), playerId)));
		} else if (event instanceof AfterSpellCastedEvent) {
			final AfterSpellCastedEvent afterSpellCastedEvent = (AfterSpellCastedEvent) event;
			final Card card = afterSpellCastedEvent.getSourceCard();
			com.hiddenswitch.spellsource.client.models.Entity entity = getEntity(workingContext, card, playerId);
			if (card.getCardType() == CardType.SPELL
					&& card instanceof SecretCard
					&& card.getOwner() != playerId) {
				entity = getCensoredCard(card.getId(), card.getOwner(), card.getEntityLocation(), card.getHeroClass());
			}

			clientEvent.afterSpellCasted(new GameEventAfterSpellCasted()
					.sourceCard(entity)
					.spellTarget(getEntity(workingContext, afterSpellCastedEvent.getEventTarget(), playerId)));
		} else if (event instanceof SecretRevealedEvent) {
			final SecretRevealedEvent secretRevealedEvent = (SecretRevealedEvent) event;
			clientEvent.secretRevealed(new GameEventSecretRevealed()
					.secret(getEntity(workingContext, secretRevealedEvent.getSecretCard(), playerId)));
		} else if (event instanceof QuestSuccessfulEvent) {
			final QuestSuccessfulEvent questSuccessfulEvent = (QuestSuccessfulEvent) event;
			clientEvent.questSuccessful(new GameEventQuestSuccessful()
					.quest(getEntity(workingContext, questSuccessfulEvent.getQuest(), playerId)));
		}

		return clientEvent;
	}

	static com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent getPhysicalAttack(GameContext workingContext, Actor attacker, Actor defender, int damageDealt, int playerId) {
		return new com.hiddenswitch.spellsource.client.models.PhysicalAttackEvent()
				.attacker(getEntity(workingContext, attacker, playerId))
				.defender(getEntity(workingContext, defender, playerId))
				.damageDealt(damageDealt);
	}

	/**
	 * Requests if this particular game service contains the game session.
	 * <p>
	 * In the future, it should punt the request to the next Games service instance if it doesn't have the requested
	 * session.
	 *
	 * @param request Information to help query for game sessions.
	 * @return A reference to the game session and useful information about it.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	ContainsGameSessionResponse containsGameSession(ContainsGameSessionRequest request) throws SuspendExecution, InterruptedException;

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
	 * Gets the current game state of a requested game ID. In the future, this method should punt the request to the
	 * next Games service instance if it can't find the given session.
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
	 * Updates an entity specified inside the game with specific attributes. Currently unsupported. This allows
	 * real-time manipulation of a game in progress. This call should punt the request to the next instance in the
	 * cluster if it does not have the specified game.
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
		DescribeGameSessionResponse gameSession = describeGameSession(new DescribeGameSessionRequest(gameId));
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
							.location(Games.toClientLocation(player.getEntityLocation())));
			playerEntities.add(playerEntity);
			final com.hiddenswitch.spellsource.client.models.Entity heroEntity = getEntity(workingContext, player.getHero(), localPlayerId);
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
				.collect(Collectors.toList()));

		// Any missing entities will get a stand-in entry
		Set<Integer> visibleEntityIds = entities.stream().map(com.hiddenswitch.spellsource.client.models.Entity::getId).collect(Collectors.toSet());
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId())).map(e -> new com.hiddenswitch.spellsource.client.models.Entity()
				.id(e.getId())
				.cardId("hidden")
				.state(new EntityState()
						.location(toClientLocation(e.getEntityLocation())))
				.entityType(com.hiddenswitch.spellsource.client.models.Entity.EntityTypeEnum.valueOf(e.getEntityType().toString()))).collect(Collectors.toList()));

		return new GameState()
				.isLocalPlayerTurn(localPlayerId == workingContext.getActivePlayerId())
				.entities(entities)
				.turnNumber(workingContext.getTurn())
				.timestamp(System.nanoTime())
				.turnState(workingContext.getTurnState().toString());
	}

	/**
	 * Gets a client view of the specified game engine entity. Tries its best to not leak information given the
	 * specified user.
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
				.description(card.getDescription())
				.name(card.getName())
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
				|| actor.hasAttribute(Attribute.HP_BONUS)
				|| actor.hasAttribute(Attribute.ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
				|| actor.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
		entityState.frozen(actor.hasAttribute(Attribute.FROZEN));
		entityState.immune(actor.hasAttribute(Attribute.IMMUNE) || actor.hasAttribute(Attribute.IMMUNE_WHILE_ATTACKING));
		entityState.stealth(actor.hasAttribute(Attribute.STEALTH));
		entityState.taunt(actor.hasAttribute(Attribute.TAUNT));
		entityState.divineShield(actor.hasAttribute(Attribute.DIVINE_SHIELD));
		entityState.enraged(actor.hasAttribute(Attribute.ENRAGED));
		entityState.destroyed(actor.hasAttribute(Attribute.DESTROYED));
		entityState.cannotAttack(actor.hasAttribute(Attribute.CANNOT_ATTACK));
		entityState.spellDamage(actor.getAttributeValue(Attribute.SPELL_DAMAGE));
		entityState.windfury(actor.hasAttribute(Attribute.WINDFURY));
		entityState.summoningSickness(actor.hasAttribute(Attribute.SUMMONING_SICKNESS));
		entityState.untargetableBySpells(actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		final List<Trigger> triggers = workingContext.getTriggerManager().getTriggersAssociatedWith(actor.getReference());
		entityState.hostsTrigger(triggers.size() > 0);
		entity.state(entityState);
		return entity;
	}

	/**
	 * A view of a secret or quest. Censors information from opposing players if it's a quest.
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

		com.hiddenswitch.spellsource.client.models.Entity cardEntity = getEntity(workingContext, enchantment.getSourceCard(), localPlayerId);
		if (enchantment instanceof Secret
				&& localPlayerId != enchantment.getOwner()) {
			// Censor information about the secret if it does not belong to the player.
			cardEntity
					.name("Secret")
					.description("Secret")
					.cardId("hidden");
		}
		Entity.EntityTypeEnum entityType = Entity.EntityTypeEnum.SECRET;
		if (enchantment instanceof Quest) {
			entityType = Entity.EntityTypeEnum.QUEST;
		}

		cardEntity.id(enchantment.getId())
				.entityType(entityType)
				.getState()
				.location(Games.toClientLocation(enchantment.getEntityLocation()))
				.owner(enchantment.getOwner())
				.playable(false);
		return cardEntity;
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
			final boolean playable = workingContext.getLogic().canPlayCard(owner, card.getCardReference())
					&& card.getOwner() == workingContext.getActivePlayerId()
					&& localPlayerId == card.getOwner();
			entityState.playable(playable);
			entityState.manaCost(workingContext.getLogic().getModifiedManaCost(workingContext.getPlayer(owner), card));
			owningPlayer = workingContext.getPlayer(card.getOwner());
		} else {
			entityState.playable(false);
			entityState.manaCost(card.getBaseManaCost());
			owningPlayer = Player.empty();
		}

		entityState.owner(card.getOwner());
		entityState.location(Games.toClientLocation(card.getEntityLocation()));
		entityState.baseManaCost(card.getBaseManaCost());
		entityState.battlecry(card.hasAttribute(Attribute.BATTLECRY));
		entityState.deathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
		entityState.heroClass(card.getHeroClass().toString());
		entityState.cardType(EntityState.CardTypeEnum.valueOf(card.getCardType().toString()));
		final boolean hostsTrigger = workingContext.getTriggerManager().getTriggersAssociatedWith(card.getReference()).size() > 0;
		// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
		switch (card.getCardType()) {
			case HERO:
				HeroCard heroCard = (HeroCard) card;
				// Retrieve the weapon attack
				WeaponCard heroWeaponCard = heroCard.getWeapon();
				if (heroWeaponCard != null) {
					entityState.attack(heroWeaponCard.getBaseDamage());
				}
				entityState.armor(heroCard.getArmor());
				break;
			case MINION:
				MinionCard minionCard = (MinionCard) card;
				entityState.attack(minionCard.getAttack() + minionCard.getBonusAttack());
				entityState.baseAttack(minionCard.getBaseAttack());
				entityState.baseManaCost(minionCard.getBaseManaCost());
				entityState.hp(minionCard.getHp());
				entityState.baseHp(minionCard.getBaseHp());
				entityState.maxHp(minionCard.getBaseHp() + minionCard.getBonusHp());
				entityState.underAura(minionCard.getBonusAttack() > 0
						|| minionCard.getBonusAttack() > 0
						|| hostsTrigger);
				break;
			case WEAPON:
				WeaponCard weaponCard = (WeaponCard) card;
				entityState.durability(weaponCard.getDurability());
				entityState.hp(weaponCard.getDurability());
				entityState.maxHp(weaponCard.getBaseDurability() + weaponCard.getBonusDurability());
				entityState.attack(weaponCard.getDamage() + weaponCard.getBonusDamage());
				entityState.underAura(weaponCard.getBonusDamage() > 0
						|| weaponCard.getBonusDurability() > 0
						|| hostsTrigger);
				break;
			case SPELL:
			case HERO_POWER:
				SpellCard spellCard = (SpellCard) card;
				int damage = 0;
				int spellpowerDamage = 0;
				if (DamageSpell.class.isAssignableFrom(spellCard.getSpell().getSpellClass())
						&& owningPlayer != null) {
					// Use a zero zero minion as the target entity
					final MinionCardDesc desc = new MinionCardDesc();
					desc.baseAttack = 0;
					desc.baseHp = 0;
					Minion zeroZero = ((MinionCard) desc.createInstance()).summon();
					zeroZero.setId(65535);
					damage = DamageSpell.getDamage(workingContext, owningPlayer, spellCard.getSpell(), card, zeroZero);
					spellpowerDamage = workingContext.getLogic().applySpellpower(owningPlayer, spellCard, damage);
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
}
