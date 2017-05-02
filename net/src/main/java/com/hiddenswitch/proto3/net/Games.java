package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.GameEvent;
import com.hiddenswitch.proto3.net.client.models.PhysicalAttackEvent;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.events.*;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bberman on 12/8/16.
 */
public interface Games {
	long DEFAULT_NO_ACTIVITY_TIMEOUT = 180000L;
	String WEBSOCKET_PATH = "games";

	static GameActions getClientActions(GameContext workingContext, List<GameAction> actions, int playerId) {
		final GameActions clientActions = new GameActions();

		// Get the minions targeted
		Map<Integer, Integer> minions = workingContext.getEntities()
				.filter(e -> e.getEntityType() == EntityType.MINION)
				.collect(Collectors.toMap(net.demilich.metastone.game.entities.Entity::getId, e -> e.getEntityLocation().getIndex()));

		// Battlecries
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.BATTLECRY)
				.map(ga -> (BattlecryAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getSource().getId()))
				.entrySet()
				.stream()
				.map(kv -> {
					SpellAction spellAction = new SpellAction()
							.sourceId(kv.getKey());

					// Targetable battlecry
					kv.getValue().stream()
							.map(t -> new TargetActionPair()
									.action(t.getId())
									.target(t.getTargetKey().getId()))
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
					SpellAction spellAction = new SpellAction()
							.sourceId(kv.getKey());

					// Targetable spell
					if (kv.getValue().size() == 1
							&& (kv.getValue().get(0).getTargetKey() == null
							|| kv.getValue().get(0).getTargetKey().isTargetGroup())) {
						spellAction.action(kv.getValue().get(0).getId());
					} else {
						// Add all the valid targets
						kv.getValue().stream()
								.map(t -> new TargetActionPair()
										.action(t.getId())
										.target(t.getTargetKey().getId()))
								.forEach(spellAction::addTargetKeyToActionsItem);
					}

					return spellAction;
				})
				.forEach(clientActions::addSpellsItem);

		// Choose one summons are actually play one cards with the same action
		actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.SUMMON)
				.map(ga -> (PlayMinionCardAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getCardId()))
				.entrySet()
				.stream()
				.map(kv -> {
					SummonAction summonAction = new SummonAction()
							.sourceId(kv.getKey())
							.indexToActions(kv.getValue().stream()
									.filter(a -> a.getTargetKey() != null)
									.map(a -> new SummonActionIndexToActions()
											.action(a.getId())
											.index(minions.get(a.getTargetKey().getId()))).collect(Collectors.toList()));

					// Add the null targeted action, if it exists
					Optional<PlayMinionCardAction> nullPlay = kv.getValue().stream()
							.filter(a -> a.getTargetKey() == null).findFirst();
					if (nullPlay.isPresent()) {
						GameAction a = nullPlay.get();
						summonAction.addIndexToActionsItem(
								new SummonActionIndexToActions()
										.action(a.getId())
										.index(workingContext.getPlayer(playerId).getMinions().size()));
					}

					return summonAction;
				}).forEach(clientActions::addSummonsItem);

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
								new TargetActionPair().target(ga.getTargetKey().getId())
										.action(ga.getId())
						).collect(Collectors.toList())))
				.forEach(clientActions::addPhysicalAttacksItem);

		// Hero powers
		Optional<SpellAction> heroPowerSpell = actions.stream()
				.filter(ga -> ga.getActionType() == ActionType.HERO_POWER)
				.map(ga -> (HeroPowerAction) ga)
				.collect(Collectors.groupingBy(ga -> ga.getCardReference().getCardId()))
				.entrySet()
				.stream()
				.map(kv -> {
					SpellAction spellAction = new SpellAction()
							.sourceId(kv.getKey());

					// Targetable spell
					if (kv.getValue().size() == 1
							&& (kv.getValue().get(0).getTargetKey() == null
							|| kv.getValue().get(0).getTargetKey().isTargetGroup())) {
						spellAction.action(kv.getValue().get(0).getId());
					} else {
						// Add all the valid targets
						kv.getValue().stream()
								.map(t -> new TargetActionPair()
										.action(t.getId())
										.target(t.getTargetKey().getId()))
								.forEach(spellAction::addTargetKeyToActionsItem);
					}

					return spellAction;
				}).findFirst();

		if (heroPowerSpell.isPresent()) {
			clientActions.heroPower(heroPowerSpell.get());
		}

		Optional<EndTurnAction> endTurnAction = actions
				.stream()
				.filter(ga -> ga.getActionType() == ActionType.END_TURN)
				.map(ga -> (EndTurnAction) ga)
				.findFirst();

		if (endTurnAction.isPresent()) {
			clientActions.endTurn(endTurnAction.get().getId());
		}

		// Add all the action indices for compatibility purposes
		clientActions.compatibility(actions.stream()
				.map(GameAction::getId)
				.collect(Collectors.toList()));

		return clientActions;
	}

	static GameEvent getClientEvent(net.demilich.metastone.game.events.GameEvent event, int playerId) {
		final GameEvent clientEvent = new GameEvent();

		clientEvent.eventType(GameEvent.EventTypeEnum.valueOf(event.getEventType().toString()));

		GameContext workingContext = event.getGameContext().clone();
		// Handle the event types here.
		if (event instanceof net.demilich.metastone.game.events.PhysicalAttackEvent) {
			final net.demilich.metastone.game.events.PhysicalAttackEvent physicalAttackEvent
					= (net.demilich.metastone.game.events.PhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt, playerId);
			clientEvent.physicalAttack(physicalAttack);
		} else if (event instanceof AfterPhysicalAttackEvent) {
			final AfterPhysicalAttackEvent physicalAttackEvent = (AfterPhysicalAttackEvent) event;
			final Actor attacker = physicalAttackEvent.getAttacker();
			final Actor defender = physicalAttackEvent.getDefender();
			final int damageDealt = physicalAttackEvent.getDamageDealt();
			final PhysicalAttackEvent physicalAttack = getPhysicalAttack(workingContext, attacker, defender, damageDealt, playerId);
			clientEvent.afterPhysicalAttack(physicalAttack);
		} else if (event instanceof DrawCardEvent) {
			final DrawCardEvent drawCardEvent = (DrawCardEvent) event;
			clientEvent.drawCard(new GameEventDrawCard()
					.card(getEntity(workingContext, drawCardEvent.getCard(), playerId))
					.drawn(drawCardEvent.isDrawn()));
		} else if (event instanceof KillEvent) {
			final KillEvent killEvent = (KillEvent) event;
			final net.demilich.metastone.game.entities.Entity victim = killEvent.getVictim();
			final Entity entity = getEntity(workingContext, victim, playerId);

			clientEvent.kill(new GameEventKill()
					.victim(entity));
		} else if (event instanceof CardPlayedEvent) {
			final CardPlayedEvent cardPlayedEvent = (CardPlayedEvent) event;
			final Card card = cardPlayedEvent.getCard();
			clientEvent.cardPlayed(new GameEventCardPlayed()
					.card(getEntity(workingContext, card, playerId)));
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
			clientEvent.afterSpellCasted(new GameEventAfterSpellCasted()
					.sourceCard(getEntity(workingContext, afterSpellCastedEvent.getSourceCard(), playerId))
					.spellTarget(getEntity(workingContext, afterSpellCastedEvent.getEventTarget(), playerId)));
		}

		clientEvent.eventSource(getEntity(workingContext, event.getEventSource(), playerId));
		clientEvent.eventTarget(getEntity(workingContext, event.getEventTarget(), playerId));
		clientEvent.targetPlayerId(event.getTargetPlayerId());
		clientEvent.sourcePlayerId(event.getSourcePlayerId());
		return clientEvent;
	}

	static PhysicalAttackEvent getPhysicalAttack(GameContext workingContext, Actor attacker, Actor defender, int damageDealt, int playerId) {
		return new PhysicalAttackEvent()
				.attacker(getEntity(workingContext, attacker, playerId))
				.defender(getEntity(workingContext, defender, playerId))
				.damageDealt(damageDealt);
	}

	@Suspendable
	ContainsGameSessionResponse containsGameSession(ContainsGameSessionRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	CreateGameSessionResponse createGameSession(CreateGameSessionRequest request) throws SuspendExecution, InterruptedException;

	DescribeGameSessionResponse describeGameSession(DescribeGameSessionRequest request);

	EndGameSessionResponse endGameSession(EndGameSessionRequest request) throws InterruptedException, SuspendExecution;

	@Suspendable
	UpdateEntityResponse updateEntity(UpdateEntityRequest request);

	@Suspendable
	ConcedeGameSessionResponse concedeGameSession(ConcedeGameSessionRequest request) throws InterruptedException, SuspendExecution;

	default GameState getClientGameState(String gameId, String userId) {
		DescribeGameSessionResponse gameSession = describeGameSession(new DescribeGameSessionRequest(gameId));
		final com.hiddenswitch.proto3.net.common.GameState state = gameSession.getState();
		GameContext workingContext = new GameContext();
		workingContext.loadState(state);
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

	static GameState getGameState(GameContext workingContext, final Player local, final Player opponent) {
		List<Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible
		int localPlayerId = local.getId();

		List<Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			final Entity entity = getEntity(workingContext, card, localPlayerId);
			localHand.add(entity);
		}

		// Add complete information for the local hand
		entities.addAll(localHand);

		for (EntityZone<Minion> battlefield : Arrays.asList(local.getMinions(), opponent.getMinions())) {
			List<Entity> minions = new ArrayList<>();
			for (Minion minion : battlefield) {
				final Entity entity = getEntity(workingContext, minion, localPlayerId);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
		}

		List<Entity> localSecrets = new ArrayList<>();
		// Add complete information for the local secrets
		for (Secret secret : local.getSecrets()) {
			final Entity entity = getEntity(workingContext, secret, localPlayerId);
			localSecrets.add(entity);
		}

		entities.addAll(localSecrets);

		// Add limited information for opposing secrets
		List<Entity> opposingSecrets = new ArrayList<>();
		for (Secret secret : opponent.getSecrets()) {
			final Entity entity = new Entity()
					.id(secret.getId())
					.entityType(Entity.EntityTypeEnum.SECRET)
					.state(new EntityState()
							.owner(secret.getOwner())
							.heroClass(secret.getSource().getHeroClass().toString()));
			opposingSecrets.add(entity);
		}

		entities.addAll(opposingSecrets);

		List<Entity> playerEntities = new ArrayList<>();
		// Create the heroes
		for (final Player player : Arrays.asList(local, opponent)) {
			Entity playerEntity = new Entity()
					.id(player.getId())
					.name(player.getName())
					.entityType(Entity.EntityTypeEnum.PLAYER)
					.state(new EntityState()
							.owner(player.getId())
							.lockedMana(player.getLockedMana())
							.maxMana(player.getMaxMana())
							.mana(player.getMana())
							.location(Games.toClientLocation(player.getEntityLocation())));
			playerEntities.add(playerEntity);
			final Entity heroEntity = getEntity(workingContext, player.getHero(), localPlayerId);
			// Include the player's mana, locked mana and max mana in the hero entity for convenience
			heroEntity.getState()
					.mana(player.getMana())
					.maxMana(player.getMaxMana())
					.lockedMana(player.getLockedMana());
			playerEntities.add(heroEntity);
			final Entity heroPowerEntity = getEntity(workingContext, player.getHero().getHeroPower(), localPlayerId);
			playerEntities.add(heroPowerEntity);
			if (player.getHero().getWeapon() != null) {
				final Entity weaponEntity = getEntity(workingContext, player.getHero().getWeapon(), localPlayerId);
				playerEntities.add(weaponEntity);
			}
		}

		entities.addAll(playerEntities);

		// Any missing entities will get a stand-in entry
		Set<Integer> visibleEntityIds = entities.stream().map(Entity::getId).collect(Collectors.toSet());
		entities.addAll(workingContext.getEntities().filter(e -> !visibleEntityIds.contains(e.getId())).map(e -> new Entity()
				.id(e.getId())
				.cardId("hidden")
				.state(new EntityState()
						.location(toClientLocation(e.getEntityLocation())))
				.entityType(Entity.EntityTypeEnum.valueOf(e.getEntityType().toString()))).collect(Collectors.toList()));

		final List<GameEvent> eventStack = workingContext.getEventStack().stream()
				.map(e -> Games.getClientEvent(e, localPlayerId)).collect(Collectors.toList());
		return new GameState()
				.eventStack(eventStack)
				.entities(entities)
				.timestamp(System.nanoTime())
				.turnState(workingContext.getTurnState().toString());
	}

	static Entity getEntity(final GameContext workingContext, final net.demilich.metastone.game.entities.Entity entity, int localPlayerId) {
		if (entity == null) {
			return null;
		}

		if (entity instanceof Actor) {
			return getEntity(workingContext, (Actor) entity, localPlayerId);
		} else if (entity instanceof Card) {
			return getEntity(workingContext, (Card) entity, localPlayerId);
		} else if (entity instanceof Secret) {
			return getEntity(workingContext, (Secret) entity, localPlayerId);
		}

		return null;
	}

	static Entity getEntity(final GameContext workingContext, final Actor actor, int localPlayerId) {
		if (actor == null) {
			return null;
		}

		final Card card = actor.getSourceCard();
		final EntityState entityState = new EntityState();
		final Entity entity = new Entity()
				.description(card.getDescription())
				.name(card.getName())
				.id(actor.getId())
				.cardId(card.getCardId());

		if (actor instanceof Minion) {
			entity.setEntityType(Entity.EntityTypeEnum.MINION);
			entityState.boardPosition(actor.getEntityLocation().getIndex());
		} else if (actor instanceof Hero) {
			entity.setEntityType(Entity.EntityTypeEnum.HERO);
			entityState.armor(actor.getArmor());
		} else if (actor instanceof Weapon) {
			entity.setEntityType(Entity.EntityTypeEnum.WEAPON);
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
				&& actor.canAttackThisTurn();
		entityState.playable(playable);
		entityState.attack(actor.getAttack());
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
		entityState.untargetableBySpells(actor.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
		entity.state(entityState);
		return entity;
	}

	static Entity getEntity(final GameContext workingContext, final Secret secret, int localPlayerId) {
		if (secret == null) {
			return null;
		}

		Entity cardEntity = getEntity(workingContext, secret.getSource(), localPlayerId);
		cardEntity.id(secret.getId())
				.entityType(Entity.EntityTypeEnum.SECRET)
				.getState()
				.location(Games.toClientLocation(secret.getEntityLocation()))
				.owner(secret.getOwner())
				.playable(false);
		return cardEntity;
	}

	static Entity getEntity(final GameContext workingContext, final Card card, int localPlayerId) {
		if (card == null) {
			return null;
		}

		final Entity entity = new Entity()
				.description(card.getDescription())
				.entityType(Entity.EntityTypeEnum.CARD)
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
		final boolean hostsTrigger = workingContext.getTriggerManager().getTriggersAssociatedWith(card.getReference()).size() > 0;
		// TODO: Run the game context to see if the card has any triggering side effects. If it does, then color its border yellow.
		switch (card.getCardType()) {
			case MINION:
				MinionCard minionCard = (MinionCard) card;
				entityState.attack(minionCard.getAttack() + minionCard.getBonusAttack());
				entityState.baseAttack(minionCard.getBaseAttack());
				entityState.baseManaCost(minionCard.getBaseManaCost());
				entityState.hp(minionCard.getHp());
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

	static EntityLocation toClientLocation(net.demilich.metastone.game.entities.EntityLocation location) {
		return new EntityLocation()
				.zone(EntityLocation.ZoneEnum.valueOf(location.getZone().toString()))
				.index(location.getIndex())
				.player(location.getPlayer());
	}

	static long getDefaultNoActivityTimeout() {
		return Long.parseLong(System.getProperties().getProperty("games.defaultNoActivityTimeout", Long.toString(Games.DEFAULT_NO_ACTIVITY_TIMEOUT)));
	}
}
