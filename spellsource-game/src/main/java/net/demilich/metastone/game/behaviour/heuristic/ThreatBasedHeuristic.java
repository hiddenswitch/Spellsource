package net.demilich.metastone.game.behaviour.heuristic;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
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
			damageOnBoard += minion.getAttack() * minion.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
		}
		damageOnBoard += getHeroDamage(opponent.getHero(), player);

		int remainingHp = player.getHero().getEffectiveHp() - damageOnBoard;
		boolean observesLethal = GameStateValueBehaviour.observesLethal(context, opponent.getId(), player.getHero());
		if (remainingHp < 1 || observesLethal) {
			return ThreatLevel.RED;
		} else if (remainingHp < 15) {
			return ThreatLevel.YELLOW;
		}

		return ThreatLevel.GREEN;
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
		boolean destroySpell = false;
		if (card.getDesc().getBattlecry() != null
				&& card.getDesc().getBattlecry().getSpell() != null) {
			SpellDesc spell = card.getDesc().getBattlecry().getSpell();
			destroySpell |= DestroySpell.class.isAssignableFrom(spell.getDescClass())
					|| spell.subSpells().stream().anyMatch(sd -> DestroySpell.class.isAssignableFrom(sd.getDescClass()));
		}
		if (card.getSpell() != null) {
			SpellDesc spell = card.getSpell();
			destroySpell |= DestroySpell.class.isAssignableFrom(spell.getDescClass())
					|| spell.subSpells().stream().anyMatch(sd -> DestroySpell.class.isAssignableFrom(sd.getDescClass()));
		}
		return cardCatalogue.getHardRemovalCardIds().contains(card.getCardId())
				|| isPoisonous
				|| destroySpell;
	}

	private final FeatureVector weights;

	public ThreatBasedHeuristic(FeatureVector vector) {
		this.weights = vector;
	}

	private double calculateMinionScore(Minion minion, ThreatLevel threatLevel) {
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
		if (minion.hasAttribute(Attribute.UNTARGETABLE_BY_SPELLS)) {
			minionScore += weights.get(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER);
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
		for (Card card : player.getHand()) {
			if (isHardRemoval(context.getCardCatalogue(), card)) {
				score += weights.get(WeightedFeature.HARD_REMOVAL_VALUE);
			}

			if (card.hasAttribute(Attribute.CURSE)) {
				score += weights.get(WeightedFeature.CURSED_FACTOR);
			}
		}

		score += player.getHand().getCount() * weights.get(WeightedFeature.OWN_CARD_COUNT);
		score += opponent.getHand().getCount() * weights.get(WeightedFeature.OPPONENT_CARD_COUNT);

		for (Minion minion : player.getMinions()) {
			score += calculateMinionScore(minion, threatLevel);
		}

		for (Minion minion : opponent.getMinions()) {
			score -= calculateMinionScore(minion, threatLevel);
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

		// Count roasted cards to make sure the bot punishes Fel Reaper
		score += player.getGraveyard().stream().filter(c -> c.hasAttribute(Attribute.ROASTED)).count() * weights.get(WeightedFeature.OWN_ROASTED_VALUE);
		score += opponent.getGraveyard().stream().filter(c -> c.hasAttribute(Attribute.ROASTED)).count() * weights.get(WeightedFeature.OPPONENT_ROASTED_VALUE);

		score += player.getMaxMana() * weights.get(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE);
		score += opponent.getMaxMana() * weights.get(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE);

		return score;
	}
}
