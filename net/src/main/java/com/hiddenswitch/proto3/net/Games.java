package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.client.models.Entity;
import com.hiddenswitch.proto3.net.client.models.EntityState;
import com.hiddenswitch.proto3.net.client.models.GameState;
import com.hiddenswitch.proto3.net.client.models.Zone;
import com.hiddenswitch.proto3.net.impl.util.Zones;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.WeaponCard;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.spells.DamageSpell;
import net.demilich.metastone.game.spells.trigger.IGameEventListener;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bberman on 12/8/16.
 */
public interface Games {
	long DEFAULT_NO_ACTIVITY_TIMEOUT = 180000L;
	String WEBSOCKET_PATH = "games";

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
		List<Zone> zones = new ArrayList<>();
		List<Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible


		List<Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			final Entity entity = getEntity(workingContext, card);
			localHand.add(entity);
		}

		// Add complete information for the local hand
		entities.addAll(localHand);
		zones.add(new Zone()
				.id(Zones.LOCAL_HAND)
				.entities(localHand.stream().map(Entity::getId).collect(toList())));

		for (Tuple<Integer, List<Minion>> minionTuple : Arrays.asList(new Tuple<>(Zones.LOCAL_BATTLEFIELD, local.getMinions()), new Tuple<>(Zones.OPPONENT_BATTLEFIELD, opponent.getMinions()))) {
			int zone = minionTuple.getFirst();
			List<Minion> battlefield = minionTuple.getSecond();
			List<Entity> minions = new ArrayList<>();
			for (Minion minion : battlefield) {
				final Entity entity = getEntity(workingContext, minion);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
			zones.add(new Zone()
					.id(zone)
					.entities(minions.stream().map(Entity::getId).collect(toList())));
		}

		List<Entity> localSecrets = new ArrayList<>();
		// Add complete information for the local secrets
		for (IGameEventListener iSecret : workingContext.getLogic().getSecrets(local)) {
			Secret secret = (Secret) iSecret;
			final Entity entity = getEntity(workingContext, secret);
			localSecrets.add(entity);
		}

		entities.addAll(localSecrets);
		zones.add(new Zone()
				.id(Zones.LOCAL_SECRET)
				.entities(localSecrets.stream().map(Entity::getId).collect(toList())));

		// Add limited information for opposing secrets
		List<Entity> opposingSecrets = new ArrayList<>();
		for (IGameEventListener iSecret : workingContext.getLogic().getSecrets(opponent)) {
			Secret secret = (Secret) iSecret;
			final Entity entity = new Entity()
					.id(secret.getId())
					.entityType(Entity.EntityTypeEnum.SECRET)
					.state(new EntityState()
							.heroClass(secret.getSource().getHeroClass().toString()));
			opposingSecrets.add(entity);
		}

		entities.addAll(opposingSecrets);
		zones.add(new Zone()
				.id(Zones.OPPONENT_SECRET)
				.entities(opposingSecrets.stream().map(Entity::getId).collect(toList())));

		List<Entity> playerEntities = new ArrayList<>();
		// Create the heroes
		for (Tuple<Integer, Player> heroTuple : Arrays.asList(new Tuple<>(Zones.LOCAL_HERO, local), new Tuple<>(Zones.OPPONENT_HERO, opponent))) {
			final Player player = heroTuple.getSecond();
			int heroZone = heroTuple.getFirst();
			int playerZone = Zones.LOCAL_PLAYER - Zones.LOCAL_HERO + heroZone;
			int heroPowerZone = Zones.LOCAL_HERO_POWER - Zones.LOCAL_HERO + heroZone;
			int weaponZone = Zones.LOCAL_WEAPON - Zones.LOCAL_HERO + heroZone;
			Entity playerEntity = new Entity()
					.id(player.getId())
					.name(player.getName())
					.entityType(Entity.EntityTypeEnum.PLAYER)
					.state(new EntityState()
							.lockedMana(player.getLockedMana())
							.maxMana(player.getMaxMana())
							.mana(player.getMana()));
			playerEntities.add(playerEntity);
			final Entity heroEntity = getEntity(workingContext, player.getHero());
			playerEntities.add(heroEntity);
			final Entity heroPowerEntity = getEntity(workingContext, player.getHero().getHeroPower());
			playerEntities.add(heroPowerEntity);
			if (player.getHero().getWeapon() != null) {
				final Entity weaponEntity = getEntity(workingContext, player.getHero().getWeapon());
				playerEntities.add(weaponEntity);
				zones.add(new Zone()
						.id(weaponZone)
						.addEntitiesItem(weaponEntity.getId()));
			}
			zones.add(new Zone()
					.id(heroZone)
					.addEntitiesItem(heroEntity.getId()));
			zones.add(new Zone()
					.id(playerZone)
					.addEntitiesItem(playerEntity.getId()));
			zones.add(new Zone()
					.id(heroPowerZone)
					.addEntitiesItem(heroPowerEntity.getId()));

		}

		entities.addAll(playerEntities);

		// Add IDs for the decks and the opposing hand.
		List<Entity> localDeck = local.getDeck().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());
		List<Entity> opposingDeck = opponent.getDeck().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());
		// TODO: The opposing hand should show auras
		List<Entity> opposingHand = opponent.getHand().toList().stream().map(c -> new Entity().id(c.getId())).collect(toList());

		zones.add(new Zone()
				.id(Zones.LOCAL_DECK)
				.entities(localDeck.stream().map(Entity::getId).collect(toList())));
		zones.add(new Zone()
				.id(Zones.OPPONENT_DECK)
				.entities(opposingDeck.stream().map(Entity::getId).collect(toList())));
		zones.add(new Zone()
				.id(Zones.OPPONENT_HAND)
				.entities(opposingHand.stream().map(Entity::getId).collect(toList())));


		return new GameState()
				.entities(entities)
				.zones(zones)
				.timestamp(System.nanoTime())
				.turnState(workingContext.getTurnState().toString());
	}

	static Entity getEntity(final GameContext workingContext, final Actor actor) {

		final Card card = actor.getSourceCard();
		final Entity entity = new Entity()
				.description(card.getDescription())
				.name(card.getName())
				.description(card.getDescription())
				.id(card.getId())
				.cardId(card.getCardId());
		if (actor instanceof Minion) {
			entity.setEntityType(Entity.EntityTypeEnum.MINION);
		} else if (actor instanceof Hero) {
			entity.setEntityType(Entity.EntityTypeEnum.HERO);
		} else if (actor instanceof Weapon) {
			entity.setEntityType(Entity.EntityTypeEnum.WEAPON);
		}
		final EntityState entityState = new EntityState();
		entityState.manaCost(card.getBaseManaCost());
		entityState.heroClass(card.getHeroClass().toString());
		entityState.baseManaCost(card.getBaseManaCost());
		entityState.silenced(actor.hasAttribute(Attribute.SILENCED));
		entityState.deathrattles(actor.getDeathrattles() != null);
		entityState.playable(actor.canAttackThisTurn());
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

	static Entity getEntity(final GameContext workingContext, final Secret secret) {
		Entity cardEntity = getEntity(workingContext, secret.getSource());
		cardEntity.id(secret.getId())
				.entityType(Entity.EntityTypeEnum.SECRET)
				.getState()
				.playable(false);
		return cardEntity;
	}

	static Entity getEntity(final GameContext workingContext, final Card card) {
		final Entity entity = new Entity()
				.description(card.getDescription())
				.entityType(Entity.EntityTypeEnum.CARD)
				.name(card.getName())
				.description(card.getDescription())
				.id(card.getId())
				.cardId(card.getCardId());
		final EntityState entityState = new EntityState();
		int owner = card.getOwner();
		Player localPlayer;
		if (owner != -1) {
			entityState.playable(workingContext.getLogic().canPlayCard(owner, card.getCardReference()));
			entityState.manaCost(workingContext.getLogic().getModifiedManaCost(workingContext.getPlayer(owner), card));
			localPlayer = workingContext.getPlayer(card.getOwner());
		} else {
			entityState.playable(false);
			entityState.manaCost(card.getBaseManaCost());
			localPlayer = Player.empty();
		}

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
						&& localPlayer != null) {
					// Use a zero zero minion as the target entity
					final MinionCardDesc desc = new MinionCardDesc();
					desc.baseAttack = 0;
					desc.baseHp = 0;
					Minion zeroZero = ((MinionCard) desc.createInstance()).summon();
					zeroZero.setId(65535);
					damage = DamageSpell.getDamage(workingContext, localPlayer, spellCard.getSpell(), card, zeroZero);
					spellpowerDamage = workingContext.getLogic().applySpellpower(localPlayer, spellCard, damage);
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
}
