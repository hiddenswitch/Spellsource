package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;
import net.demilich.metastone.game.cards.Attribute;

import java.io.Serializable;

/**
 * A heuristic that considers a linear combination of game state entities to determine the strength of a game state.
 */
public class ThreatBasedHeuristic implements Heuristic, Serializable {

	private static ThreatLevel calcuateThreatLevel(GameContext context, int playerId) {
		int damageOnBoard = 0;
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		for (Minion minion : opponent.getMinions()) {
			if (minion.hasAttribute(Attribute.PERMANENT)
					|| minion.hasAttribute(Attribute.DORMANT)
					|| minion.hasAttribute(Attribute.FROZEN)
					|| minion.hasAttribute(Attribute.CANNOT_ATTACK)
					|| minion.hasAttribute(Attribute.AURA_CANNOT_ATTACK)) {
				continue;
			}
			damageOnBoard += minion.getAttack() * minion.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
		}
		damageOnBoard += getHeroDamage(opponent.getHero(), opponent);

		int remainingHp = player.getHero().getEffectiveHp() - damageOnBoard;
		boolean observesLethal = GameStateValueBehaviour.observesLethal(context, opponent.getId(), player.getHero());
		if (remainingHp < 1 || observesLethal) {
			return ThreatLevel.RED;
		} else if (remainingHp < 15) {
			return ThreatLevel.YELLOW;
		}

		return ThreatLevel.GREEN;
	}

	private static boolean isLocation(Minion minion) {
		Card sourceCard = minion.getSourceCard();
		return (minion.hasAttribute(Attribute.PERMANENT) && !minion.hasAttribute(Attribute.DORMANT))
				&& sourceCard != null
				&& sourceCard.getDesc() != null
				&& sourceCard.getDesc().getTaps() != null
				&& sourceCard.getDesc().getTaps().length > 0;
	}

	private static boolean isDormantOrPermanentNonLocation(Minion minion) {
		return minion.hasAttribute(Attribute.DORMANT)
				|| (minion.hasAttribute(Attribute.PERMANENT) && !isLocation(minion));
	}

	private static int getHeroDamage(Hero hero, Player player) {
		int heroDamage = 0;
		if (hero.getHeroClass().equals("BLUE")) {
			heroDamage += 1;
		} else if (hero.getHeroClass().equals("GREEN")) {
			heroDamage += 2;
		} else if (hero.getHeroClass().equals("BROWN")) {
			heroDamage += 1;
		} else if (hero.getHeroClass().equals("BLACK")) {
			heroDamage += 1;
		}
		if (!player.getWeaponZone().isEmpty()) {
			heroDamage += player.getWeaponZone().get(0).getWeaponDamage();
		}
		return heroDamage;
	}

	private static boolean isHardRemoval(CardCatalogue cardCatalogue, Card card) {
		boolean isPoisonous = card.hasAttribute(Attribute.POISONOUS)
				|| card.hasAttribute(Attribute.AURA_POISONOUS);
		return isPoisonous
				|| card.getDesc().isHardRemoval()
				|| (card.getDesc().artificialIntelligence() != null && card.getDesc().artificialIntelligence().getHardRemoval())
				|| cardCatalogue.getHardRemovalCardIds().contains(card.getCardId());
	}

	private final FeatureVector weights;

	public ThreatBasedHeuristic(FeatureVector vector) {
		this.weights = vector;
	}

	private double calculateOwnMinionScore(Minion minion, ThreatLevel threatLevel) {
		double minionScore = weights.get(WeightedFeature.MINION_INTRINSIC_VALUE);
		minionScore += weights.get(WeightedFeature.MINION_ATTACK_FACTOR)
				* (minion.getAttack() - minion.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS));
		minionScore += weights.get(WeightedFeature.MINION_HP_FACTOR) * minion.getHp();

		if (minion.hasAttribute(Attribute.TAUNT) || minion.hasAttribute(Attribute.AURA_TAUNT)) {
			switch (threatLevel) {
				case RED:
					minionScore += weights.get(WeightedFeature.MINION_RED_TAUNT_MODIFIER);
					break;
				case YELLOW:
					minionScore += weights.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER);
					break;
				default:
					minionScore += weights.get(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER);
					break;
			}
		}

		if (minion.hasAttribute(Attribute.WINDFURY) || minion.hasAttribute(Attribute.AURA_WINDFURY)) {
			minionScore += weights.get(WeightedFeature.MINION_WINDFURY_MODIFIER);
		} else if (minion.hasAttribute(Attribute.MEGA_WINDFURY)) {
			minionScore += 2 * weights.get(WeightedFeature.MINION_WINDFURY_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.DIVINE_SHIELD)) {
			minionScore += weights.get(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.SPELL_DAMAGE) * weights.get(WeightedFeature.MINION_SPELL_POWER_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.AURA_SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.AURA_SPELL_DAMAGE) * weights.get(WeightedFeature.MINION_SPELL_POWER_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.STEALTH) || minion.hasAttribute(Attribute.AURA_STEALTH)) {
			minionScore += weights.get(WeightedFeature.MINION_STEALTHED_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
				|| minion.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
				|| minion.hasAttribute(Attribute.UNTARGETABLE_BY_OPPONENT_SPELLS)) {
			minionScore += weights.get(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.POISONOUS) || minion.hasAttribute(Attribute.AURA_POISONOUS)) {
			minionScore += weights.get(WeightedFeature.MINION_POISONOUS_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.LIFESTEAL) || minion.hasAttribute(Attribute.AURA_LIFESTEAL)) {
			minionScore += weights.get(WeightedFeature.MINION_LIFESTEAL_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.REBORN)) {
			minionScore += weights.get(WeightedFeature.MINION_REBORN_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.FROZEN)) {
			minionScore += weights.get(WeightedFeature.MINION_FROZEN_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.DEATHRATTLES)) {
			minionScore += weights.get(WeightedFeature.MINION_DEATHRATTLE_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.RUSH) || minion.hasAttribute(Attribute.AURA_RUSH)) {
			minionScore += weights.get(WeightedFeature.MINION_RUSH_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.IMMUNE) || minion.hasAttribute(Attribute.AURA_IMMUNE)) {
			minionScore += weights.get(WeightedFeature.MINION_IMMUNE_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.CANNOT_ATTACK) || minion.hasAttribute(Attribute.AURA_CANNOT_ATTACK)) {
			minionScore += weights.get(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER);
		}

		return minionScore;
	}

	private double calculateOpponentMinionScore(Minion minion, ThreatLevel threatLevel) {
		double minionScore = weights.get(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE);
		minionScore += weights.get(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR)
				* (minion.getAttack() - minion.getAttributeValue(Attribute.TEMPORARY_ATTACK_BONUS));
		minionScore += weights.get(WeightedFeature.OPPONENT_MINION_HP_FACTOR) * minion.getHp();

		if (minion.hasAttribute(Attribute.TAUNT) || minion.hasAttribute(Attribute.AURA_TAUNT)) {
			switch (threatLevel) {
				case RED:
					minionScore += weights.get(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER);
					break;
				case YELLOW:
					minionScore += weights.get(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER);
					break;
				default:
					minionScore += weights.get(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER);
					break;
			}
		}

		if (minion.hasAttribute(Attribute.WINDFURY) || minion.hasAttribute(Attribute.AURA_WINDFURY)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER);
		} else if (minion.hasAttribute(Attribute.MEGA_WINDFURY)) {
			minionScore += 2 * weights.get(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.DIVINE_SHIELD)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.SPELL_DAMAGE) * weights.get(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.AURA_SPELL_DAMAGE)) {
			minionScore += minion.getAttributeValue(Attribute.AURA_SPELL_DAMAGE) * weights.get(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.STEALTH) || minion.hasAttribute(Attribute.AURA_STEALTH)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)
				|| minion.hasAttribute(Attribute.AURA_UNTARGETABLE_BY_SPELLS)
				|| minion.hasAttribute(Attribute.UNTARGETABLE_BY_OPPONENT_SPELLS)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER);
		}

		if (minion.hasAttribute(Attribute.POISONOUS) || minion.hasAttribute(Attribute.AURA_POISONOUS)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.LIFESTEAL) || minion.hasAttribute(Attribute.AURA_LIFESTEAL)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.REBORN)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.FROZEN)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.DEATHRATTLES)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.RUSH) || minion.hasAttribute(Attribute.AURA_RUSH)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.IMMUNE) || minion.hasAttribute(Attribute.AURA_IMMUNE)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER);
		}
		if (minion.hasAttribute(Attribute.CANNOT_ATTACK) || minion.hasAttribute(Attribute.AURA_CANNOT_ATTACK)) {
			minionScore += weights.get(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER);
		}

		return minionScore;
	}

	@Override
	public double getScore(GameContext context, int playerId) {
		Player player = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		if (player.getHero().isDestroyed()) {
			return Double.NEGATIVE_INFINITY;
		}
		if (opponent.getHero().isDestroyed()) {
			return Double.POSITIVE_INFINITY;
		}
		double score = 0;

		ThreatLevel threatLevel = calcuateThreatLevel(context, playerId);
		switch (threatLevel) {
			case RED:
				score += weights.get(WeightedFeature.RED_MODIFIER);
				break;
			case YELLOW:
				score += weights.get(WeightedFeature.YELLOW_MODIFIER);
				break;
			default:
				break;
		}
		score += player.getHero().getEffectiveHp() * weights.get(WeightedFeature.OWN_HP_FACTOR);
		score += opponent.getHero().getEffectiveHp() * weights.get(WeightedFeature.OPPONENT_HP_FACTOR);
		int handBuffTotal = 0;
		int handManaDiscountTotal = 0;
		for (Card card : player.getHand()) {
			if (isHardRemoval(context.getCardCatalogue(), card)) {
				score += weights.get(WeightedFeature.HARD_REMOVAL_VALUE);
			}

			if (card.hasAttribute(Attribute.CURSE)) {
				score += weights.get(WeightedFeature.CURSED_FACTOR);
			}

			handBuffTotal += card.getAttributeValue(Attribute.ATTACK_BONUS) + card.getAttributeValue(Attribute.HP_BONUS);
			if (card.hasAttribute(Attribute.DARK_GIFT)) {
				handBuffTotal += 1;
			}
			int baseCost = card.getBaseManaCost();
			int modifiedCost = context.getLogic().getModifiedManaCost(player, card);
			if (modifiedCost < baseCost) {
				handManaDiscountTotal += (baseCost - modifiedCost);
			}
		}
		score += handBuffTotal * weights.get(WeightedFeature.HAND_BUFF_VALUE);
		score += handManaDiscountTotal * weights.get(WeightedFeature.HAND_MANA_REDUCTION_VALUE);

		score += player.getHand().getCount() * weights.get(WeightedFeature.OWN_CARD_COUNT);
		score += opponent.getHand().getCount() * weights.get(WeightedFeature.OPPONENT_CARD_COUNT);

		int ownActiveMinions = 0;
		int ownStarshipValue = 0;
		for (Minion minion : player.getMinions()) {
			if (isLocation(minion)) {
				score += (1.0 + 0.25 * Math.max(0, minion.getHp())) * weights.get(WeightedFeature.OWN_LOCATION_VALUE);
			} else if (isDormantOrPermanentNonLocation(minion)) {
				double wakingStats = Math.max(1, minion.getBaseAttack() + minion.getBaseHp()) / 4.0;
				score += wakingStats * weights.get(WeightedFeature.OWN_DORMANT_VALUE);
			} else {
				score += calculateOwnMinionScore(minion, threatLevel);
				ownActiveMinions++;
			}
			if (minion.hasAttribute(Attribute.STARSHIP_PIECE)
					|| (minion.getSourceCard() != null && minion.getSourceCard().hasAttribute(Attribute.STARSHIP_PIECE))) {
				ownStarshipValue++;
			}
		}

		int opponentActiveMinions = 0;
		for (Minion minion : opponent.getMinions()) {
			if (isLocation(minion)) {
				score += (1.0 + 0.25 * Math.max(0, minion.getHp())) * weights.get(WeightedFeature.OPPONENT_LOCATION_VALUE);
			} else if (isDormantOrPermanentNonLocation(minion)) {
				double wakingStats = Math.max(1, minion.getBaseAttack() + minion.getBaseHp()) / 4.0;
				score += wakingStats * weights.get(WeightedFeature.OPPONENT_DORMANT_VALUE);
			} else {
				score += calculateOpponentMinionScore(minion, threatLevel);
				opponentActiveMinions++;
			}
		}

		int questCount = player.getQuests().size();
		if (questCount > 0) {
			questCount += player.getQuests().get(0).getFires();
		}

		// Count triggered quests
		long questRewards = 0L;
		for (Entity e : player.getRemovedFromPlay()) {
			if (e instanceof Quest) {
				Quest quest = (Quest) e;
				if (quest.isExpired()
						&& quest.getFires() == quest.getCountUntilCast()) {
					questRewards++;
				}
			}
		}

		score += questCount * weights.get(WeightedFeature.QUEST_COUNTER_VALUE);
		score += questRewards * weights.get(WeightedFeature.QUEST_REWARD_VALUE);

		// Count roasted cards and corpses using indexed loops over graveyard to avoid stream overhead
		var playerGraveyard = player.getGraveyard();
		int ownRoasted = 0;
		int ownCorpses = 0;
		for (int i = 0, n = playerGraveyard.size(); i < n; i++) {
			Entity e = playerGraveyard.get(i);
			if (e.hasAttribute(Attribute.ROASTED)) {
				ownRoasted++;
			}
			if (e.getEntityType() == EntityType.MINION) {
				ownCorpses++;
			}
			if (e.hasAttribute(Attribute.STARSHIP_PIECE)
					|| (e.getSourceCard() != null && e.getSourceCard().hasAttribute(Attribute.STARSHIP_PIECE))) {
				ownStarshipValue++;
			}
		}
		var opponentGraveyard = opponent.getGraveyard();
		int opponentRoasted = 0;
		for (int i = 0, n = opponentGraveyard.size(); i < n; i++) {
			Entity e = opponentGraveyard.get(i);
			if (e.hasAttribute(Attribute.ROASTED)) {
				opponentRoasted++;
			}
		}
		score += ownRoasted * weights.get(WeightedFeature.OWN_ROASTED_VALUE);
		score += opponentRoasted * weights.get(WeightedFeature.OPPONENT_ROASTED_VALUE);

		score += player.getMaxMana() * weights.get(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE);
		score += opponent.getMaxMana() * weights.get(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE);

		// Armor
		score += player.getHero().getArmor() * weights.get(WeightedFeature.OWN_ARMOR_FACTOR);
		score += opponent.getHero().getArmor() * weights.get(WeightedFeature.OPPONENT_ARMOR_FACTOR);

		// Weapons
		if (!player.getWeaponZone().isEmpty()) {
			Weapon ownWeapon = player.getWeaponZone().get(0);
			score += 0.25 * ownWeapon.getWeaponDamage() * ownWeapon.getDurability() * weights.get(WeightedFeature.WEAPON_VALUE);
		}
		if (!opponent.getWeaponZone().isEmpty()) {
			Weapon oppWeapon = opponent.getWeaponZone().get(0);
			score += 0.25 * oppWeapon.getWeaponDamage() * oppWeapon.getDurability() * weights.get(WeightedFeature.OPPONENT_WEAPON_VALUE);
		}

		// Deck size
		score += player.getDeck().size() * weights.get(WeightedFeature.OWN_DECK_COUNT);
		score += opponent.getDeck().size() * weights.get(WeightedFeature.OPPONENT_DECK_COUNT);

		// Secrets
		score += player.getSecrets().size() * weights.get(WeightedFeature.OWN_SECRET_COUNT);
		score += opponent.getSecrets().size() * weights.get(WeightedFeature.OPPONENT_SECRET_COUNT);

		// Locked mana (overload)
		score += player.getLockedMana() * weights.get(WeightedFeature.LOCKED_MANA_VALUE);
		score += opponent.getLockedMana() * weights.get(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE);

		// Corpse count (graveyard minion count for Death Knight)
		score += ownCorpses * weights.get(WeightedFeature.CORPSE_COUNT_VALUE);

		// Imbue count / upgraded hero power
		int imbueCount = player.getAttributeValue(Attribute.IMBUE);
		if (!player.getHeroPowerZone().isEmpty()) {
			Card heroPower = player.getHeroPowerZone().get(0);
			imbueCount += heroPower.getAttributeValue(Attribute.IMBUE);
			if (!heroPower.isCollectible() && heroPower.getCardId() != null && !heroPower.getCardId().startsWith("hero_power_")) {
				imbueCount += 1;
			}
		}
		score += imbueCount * weights.get(WeightedFeature.IMBUE_VALUE);

		// Starship pieces / assembly progress
		score += ownStarshipValue * weights.get(WeightedFeature.STARSHIP_VALUE);

		// Board width (active minions only, excluding Locations and Dormant permanents)
		score += ownActiveMinions * weights.get(WeightedFeature.OWN_MINION_COUNT);
		score += opponentActiveMinions * weights.get(WeightedFeature.OPPONENT_MINION_COUNT);

		return score;
	}
}
