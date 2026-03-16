package net.demilich.metastone.game.behaviour.heuristic;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Values recorded for the default and Cuckoo-optimized values for {@link net.demilich.metastone.game.behaviour.GameStateValueBehaviour}'s
 * best heuristic weights.
 */
public class FeatureVector implements Cloneable, Serializable {

	public static FeatureVector getDefault() {
		FeatureVector defaultVector = new FeatureVector();
		// Threat level modifiers (own perspective: being threatened is bad)
		defaultVector.set(WeightedFeature.RED_MODIFIER, -50);
		defaultVector.set(WeightedFeature.YELLOW_MODIFIER, -10);
		// Health
		defaultVector.set(WeightedFeature.OWN_HP_FACTOR, 1);
		defaultVector.set(WeightedFeature.OPPONENT_HP_FACTOR, -1);
		// Curses in hand are bad
		defaultVector.set(WeightedFeature.CURSED_FACTOR, -25);
		// Card advantage
		defaultVector.set(WeightedFeature.OWN_CARD_COUNT, 3);
		defaultVector.set(WeightedFeature.OPPONENT_CARD_COUNT, -3);
		// Own minion keyword modifiers (positive = good for me)
		defaultVector.set(WeightedFeature.MINION_INTRINSIC_VALUE, 1);
		defaultVector.set(WeightedFeature.MINION_ATTACK_FACTOR, 1);
		defaultVector.set(WeightedFeature.MINION_HP_FACTOR, 1);
		defaultVector.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, 8);
		defaultVector.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, 4);
		defaultVector.set(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER, 2);
		defaultVector.set(WeightedFeature.MINION_WINDFURY_MODIFIER, 6);
		defaultVector.set(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER, 5);
		defaultVector.set(WeightedFeature.MINION_SPELL_POWER_MODIFIER, 3);
		defaultVector.set(WeightedFeature.MINION_STEALTHED_MODIFIER, 3);
		defaultVector.set(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, 3);
		defaultVector.set(WeightedFeature.MINION_POISONOUS_MODIFIER, 5);
		defaultVector.set(WeightedFeature.MINION_LIFESTEAL_MODIFIER, 3);
		defaultVector.set(WeightedFeature.MINION_REBORN_MODIFIER, 4);
		defaultVector.set(WeightedFeature.MINION_FROZEN_MODIFIER, -4);
		defaultVector.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, 2);
		defaultVector.set(WeightedFeature.MINION_RUSH_MODIFIER, 2);
		defaultVector.set(WeightedFeature.MINION_IMMUNE_MODIFIER, 8);
		defaultVector.set(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER, -5);
		// Opponent minion keyword modifiers (positive = bad for me, negative = good for me)
		defaultVector.set(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE, -1);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR, -1);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -1);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, -4);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, -2);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER, -2);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER, -6);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER, -5);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER, -3);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER, -4);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, -4);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER, -3);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER, -2);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER, -4);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER, 3);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER, -2);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER, 0);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER, -10);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER, 3);
		// Hard removal in hand is good
		defaultVector.set(WeightedFeature.HARD_REMOVAL_VALUE, 2);
		// Quests
		defaultVector.set(WeightedFeature.QUEST_COUNTER_VALUE, 3);
		defaultVector.set(WeightedFeature.QUEST_REWARD_VALUE, 9);
		// Mana crystals
		defaultVector.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 6.5);
		defaultVector.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -6.5);
		// Roasted cards
		defaultVector.set(WeightedFeature.OWN_ROASTED_VALUE, -15);
		defaultVector.set(WeightedFeature.OPPONENT_ROASTED_VALUE, 15);
		// Armor
		defaultVector.set(WeightedFeature.OWN_ARMOR_FACTOR, 0.5);
		defaultVector.set(WeightedFeature.OPPONENT_ARMOR_FACTOR, -0.5);
		// Weapons
		defaultVector.set(WeightedFeature.WEAPON_VALUE, 3);
		defaultVector.set(WeightedFeature.OPPONENT_WEAPON_VALUE, -3);
		// Deck size
		defaultVector.set(WeightedFeature.OWN_DECK_COUNT, 0.1);
		defaultVector.set(WeightedFeature.OPPONENT_DECK_COUNT, -0.1);
		// Secrets
		defaultVector.set(WeightedFeature.OWN_SECRET_COUNT, 3);
		defaultVector.set(WeightedFeature.OPPONENT_SECRET_COUNT, -3);
		// Overload
		defaultVector.set(WeightedFeature.LOCKED_MANA_VALUE, -5);
		defaultVector.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 5);
		// Corpses
		defaultVector.set(WeightedFeature.CORPSE_COUNT_VALUE, 0.5);
		// Board width
		defaultVector.set(WeightedFeature.OWN_MINION_COUNT, 1);
		defaultVector.set(WeightedFeature.OPPONENT_MINION_COUNT, -1);
		return defaultVector;
	}

	public static FeatureVector getFittest() {
		FeatureVector v = new FeatureVector();
		// Merged from 7-island IPOP-CMA-ES (best: 87.5%, ipop_cmaes_hof_v2, 65-dim).
		// Values averaged from top islands (I2=87.5%, I3=87.5%, I4=85.8%) where signs
		// agree; corrected where training signal was weak; rare features (secrets,
		// windfury, stealth, immune: 0-3 deck instances) given sensible defaults.
		// Threat level
		v.set(WeightedFeature.RED_MODIFIER, -51.0);
		v.set(WeightedFeature.YELLOW_MODIFIER, -30.0);
		// Health
		v.set(WeightedFeature.OWN_HP_FACTOR, 37.0);
		v.set(WeightedFeature.OPPONENT_HP_FACTOR, -59.0);
		// Card advantage
		v.set(WeightedFeature.OWN_CARD_COUNT, 33.0);
		v.set(WeightedFeature.OPPONENT_CARD_COUNT, -65.0);
		// Own minion stats (7/7 consensus on sign)
		v.set(WeightedFeature.MINION_INTRINSIC_VALUE, 8.0);
		v.set(WeightedFeature.MINION_ATTACK_FACTOR, 46.0);
		v.set(WeightedFeature.MINION_HP_FACTOR, 37.0);
		// Own minion keywords
		v.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, -41.0);   // 7/7 negative: in RED despite taunt = losing
		v.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, 41.0);
		v.set(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER, 23.0);
		v.set(WeightedFeature.MINION_WINDFURY_MODIFIER, 30.0);     // rare (3 cards)
		v.set(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER, 40.0);
		v.set(WeightedFeature.MINION_SPELL_POWER_MODIFIER, 11.0);
		v.set(WeightedFeature.MINION_STEALTHED_MODIFIER, 32.0);    // rare (2 cards)
		v.set(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, 10.0);
		v.set(WeightedFeature.MINION_POISONOUS_MODIFIER, 37.0);
		v.set(WeightedFeature.MINION_LIFESTEAL_MODIFIER, 35.0);
		v.set(WeightedFeature.MINION_REBORN_MODIFIER, 35.0);
		v.set(WeightedFeature.MINION_FROZEN_MODIFIER, -10.0);
		v.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, -20.0); // 6/7 negative: body understatted for cost
		v.set(WeightedFeature.MINION_RUSH_MODIFIER, -20.0);        // 5/7 negative: rush spent on play turn
		v.set(WeightedFeature.MINION_IMMUNE_MODIFIER, 27.0);       // rare (3 cards)
		v.set(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER, -20.0);
		// Hand/deck/resource
		v.set(WeightedFeature.CURSED_FACTOR, -26.0);
		v.set(WeightedFeature.HARD_REMOVAL_VALUE, 5.0);
		v.set(WeightedFeature.QUEST_COUNTER_VALUE, 3.0);
		v.set(WeightedFeature.QUEST_REWARD_VALUE, 10.0);
		v.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 41.0);
		v.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -20.0);
		v.set(WeightedFeature.OWN_ROASTED_VALUE, -45.0);
		v.set(WeightedFeature.OPPONENT_ROASTED_VALUE, 20.0);
		v.set(WeightedFeature.OWN_ARMOR_FACTOR, 31.0);
		v.set(WeightedFeature.WEAPON_VALUE, 36.0);
		v.set(WeightedFeature.OWN_DECK_COUNT, 20.0);
		v.set(WeightedFeature.OPPONENT_DECK_COUNT, -79.0);
		v.set(WeightedFeature.OWN_SECRET_COUNT, 20.0);             // rare (0 secret decks)
		v.set(WeightedFeature.OPPONENT_SECRET_COUNT, -20.0);       // rare (0 secret decks)
		v.set(WeightedFeature.LOCKED_MANA_VALUE, -30.0);
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 20.0);
		v.set(WeightedFeature.CORPSE_COUNT_VALUE, 28.0);
		// Board width
		v.set(WeightedFeature.OWN_MINION_COUNT, 48.0);
		v.set(WeightedFeature.OPPONENT_MINION_COUNT, -36.0);
		// Opponent minion stats
		v.set(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE, -20.0);
		v.set(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR, -56.0);
		v.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -12.0);
		// Opponent minion keywords
		v.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, -51.0);
		v.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, -27.0);
		v.set(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER, -38.0);
		v.set(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER, -22.0);    // rare (3 cards)
		v.set(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER, -29.0);
		v.set(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER, -49.0);
		v.set(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER, -20.0);   // rare (2 cards)
		v.set(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, -70.0);
		v.set(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER, -20.0);
		v.set(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER, -50.0);
		v.set(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER, -50.0);
		v.set(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER, 30.0);
		v.set(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER, -15.0);
		v.set(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER, -23.0);
		v.set(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER, -50.0);      // rare (3 cards)
		v.set(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER, 20.0);
		// Opponent weapon/armor
		v.set(WeightedFeature.OPPONENT_WEAPON_VALUE, -40.0);
		v.set(WeightedFeature.OPPONENT_ARMOR_FACTOR, -21.0);
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 20.0);
		return v;
	}

	private final Map<WeightedFeature, Double> values = new EnumMap<WeightedFeature, Double>(WeightedFeature.class);

	public FeatureVector() {
		for (WeightedFeature feature : WeightedFeature.values()) {
			set(feature, 0);
		}
	}

	@Override
	public FeatureVector clone() {
		FeatureVector clone = new FeatureVector();
		for (WeightedFeature feature : getValues().keySet()) {
			clone.set(feature, get(feature));
		}
		return clone;
	}

	public double get(WeightedFeature param) {
		return values.get(param);
	}

	public Map<WeightedFeature, Double> getValues() {
		return values;
	}

	public void set(WeightedFeature param, double value) {
		getValues().put(param, value);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[FeatureVector] Values:\n");
		for (WeightedFeature feature : getValues().keySet()) {
			builder.append("\t");
			builder.append(feature.toString());
			builder.append(": ");
			builder.append(String.valueOf(getValues().get(feature)));
			builder.append("\n");
		}
		return builder.toString();
	}

}
