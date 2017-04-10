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
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DamageSpell;
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

		final Player local;
		final Player opponent;
		if (state.player1.getUserId().equals(userId)) {
			local = state.player1;
			opponent = state.player2;
		} else {
			local = state.player2;
			opponent = state.player1;
		}

		List<Zone> zones = new ArrayList<>();
		List<Entity> entities = new ArrayList<>();
		// Censor the opponent hand and deck entities
		// All minions are visible
		// Heroes and players are visible

		GameContext workingContext = new GameContext();
		workingContext.loadState(state);

		List<Entity> localHand = new ArrayList<>();
		for (Card card : local.getHand()) {
			final Entity entity = new Entity()
					.description(card.getDescription())
					.entityType(Entity.EntityTypeEnum.CARD)
					.name(card.getName())
					.description(card.getDescription())
					.id(card.getId())
					.cardId(card.getCardId());
			final EntityState entityState = new EntityState();
			entityState.playable(workingContext.getLogic().canPlayCard(card.getOwner(), card.getCardReference()));
			final Player localPlayer = workingContext.getPlayer(card.getOwner());
			entityState.manaCost(workingContext.getLogic().getModifiedManaCost(localPlayer, card));
			entityState.baseManaCost(card.getBaseManaCost());
			entityState.battlecry(card.hasAttribute(Attribute.BATTLECRY));
			entityState.deathrattles(card.hasAttribute(Attribute.DEATHRATTLES));
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
					SpellCard spellCard = (SpellCard) card;
					int damage = 0;
					int spellpowerDamage = 0;
					if (DamageSpell.class.isAssignableFrom(spellCard.getSpell().getSpellClass())) {
						damage = DamageSpell.getDamage(workingContext, localPlayer, spellCard.getSpell(), card, null);
						spellpowerDamage = workingContext.getLogic().applySpellpower(localPlayer, spellCard, damage);
					}
					entityState.underAura(spellpowerDamage > damage
							|| hostsTrigger);
					entityState.spellDamage(spellpowerDamage);
					break;
				case CHOOSE_ONE:
					// Do nothing special for choose one cards
					break;
			}
			entity.state(entityState);
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
				final Card card = minion.getSourceCard();
				final Entity entity = new Entity()
						.description(card.getDescription())
						.entityType(Entity.EntityTypeEnum.MINION)
						.name(card.getName())
						.description(card.getDescription())
						.id(card.getId())
						.cardId(card.getCardId());
				final EntityState entityState = new EntityState();
				entityState.playable(workingContext.getLogic().canPlayCard(card.getOwner(), card.getCardReference()));
				final Player localPlayer = workingContext.getPlayer(card.getOwner());
				entityState.manaCost(workingContext.getLogic().getModifiedManaCost(localPlayer, card));
				entityState.baseManaCost(card.getBaseManaCost());
				entityState.silenced(minion.hasAttribute(Attribute.SILENCED));
				entityState.deathrattles(minion.getDeathrattles() != null);
				entityState.playable(minion.canAttackThisTurn());
				entityState.attack(minion.getAttack());
				entityState.hp(minion.getHp());
				entityState.maxHp(minion.getMaxHp());
				entityState.underAura(minion.hasAttribute(Attribute.AURA_ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.AURA_HP_BONUS)
						|| minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
						|| minion.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
						|| minion.hasAttribute(Attribute.HP_BONUS)
						|| minion.hasAttribute(Attribute.ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.CONDITIONAL_ATTACK_BONUS)
						|| minion.hasAttribute(Attribute.TEMPORARY_ATTACK_BONUS));
				entityState.frozen(minion.hasAttribute(Attribute.FROZEN));
				entityState.stealth(minion.hasAttribute(Attribute.STEALTH));
				entityState.taunt(minion.hasAttribute(Attribute.TAUNT));
				entityState.divineShield(minion.hasAttribute(Attribute.DIVINE_SHIELD));
				entityState.enraged(minion.hasAttribute(Attribute.ENRAGED));
				entityState.destroyed(minion.hasAttribute(Attribute.DESTROYED));
				entityState.cannotAttack(minion.hasAttribute(Attribute.CANNOT_ATTACK));
				entityState.spellDamage(minion.getAttributeValue(Attribute.SPELL_DAMAGE));
				entityState.windfury(minion.hasAttribute(Attribute.WINDFURY));
				entityState.untargetableBySpells(minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS));
				entity.state(entityState);
				minions.add(entity);
			}

			// Add complete information for the battlefield
			entities.addAll(minions);
			zones.add(new Zone()
					.id(zone)
					.entities(minions.stream().map(Entity::getId).collect(toList())));
		}

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
}
