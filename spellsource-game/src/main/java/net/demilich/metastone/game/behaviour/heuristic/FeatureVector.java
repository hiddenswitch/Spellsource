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
		// Trained via IPOP-CMA-ES (85.8% raw, island 4, 65-dim, ipop_cmaes_hof_v2)
		// Signs corrected where training signal was insufficient;
		// rare features (0-3 deck instances) given sensible magnitudes.
		// Threat level
		v.set(WeightedFeature.RED_MODIFIER, -66.553);
		v.set(WeightedFeature.YELLOW_MODIFIER, 36.460);
		// Health
		v.set(WeightedFeature.OWN_HP_FACTOR, 15.873);
		v.set(WeightedFeature.OPPONENT_HP_FACTOR, -95.412);
		// Card advantage
		v.set(WeightedFeature.OWN_CARD_COUNT, 60.154);
		v.set(WeightedFeature.OPPONENT_CARD_COUNT, -64.930);
		// Own minion stats
		v.set(WeightedFeature.MINION_INTRINSIC_VALUE, 3.774);
		v.set(WeightedFeature.MINION_ATTACK_FACTOR, 8.600);
		v.set(WeightedFeature.MINION_HP_FACTOR, 22.118);
		// Own minion keywords
		v.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, 57.455);
		v.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, 25.576);
		v.set(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER, 19.269);
		v.set(WeightedFeature.MINION_WINDFURY_MODIFIER, 50.487);
		v.set(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER, 57.090);
		v.set(WeightedFeature.MINION_SPELL_POWER_MODIFIER, 19.895);
		v.set(WeightedFeature.MINION_STEALTHED_MODIFIER, 55.751);
		v.set(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, 5.678);
		v.set(WeightedFeature.MINION_POISONOUS_MODIFIER, 17.395);
		v.set(WeightedFeature.MINION_LIFESTEAL_MODIFIER, 48.507);
		v.set(WeightedFeature.MINION_REBORN_MODIFIER, 22.470);
		v.set(WeightedFeature.MINION_FROZEN_MODIFIER, -27.136);
		v.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, 20.385);
		v.set(WeightedFeature.MINION_RUSH_MODIFIER, 20.831);
		v.set(WeightedFeature.MINION_IMMUNE_MODIFIER, 62.493);
		v.set(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER, -31.279);
		// Hand/deck/resource
		v.set(WeightedFeature.CURSED_FACTOR, -26.327);
		v.set(WeightedFeature.HARD_REMOVAL_VALUE, 3.029);
		v.set(WeightedFeature.QUEST_COUNTER_VALUE, 2.945);
		v.set(WeightedFeature.QUEST_REWARD_VALUE, 71.150);
		v.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 56.014);
		v.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -20.392);
		v.set(WeightedFeature.OWN_ROASTED_VALUE, -13.107);
		v.set(WeightedFeature.OPPONENT_ROASTED_VALUE, 58.768);
		v.set(WeightedFeature.OWN_ARMOR_FACTOR, 42.708);
		v.set(WeightedFeature.WEAPON_VALUE, 58.292);
		v.set(WeightedFeature.OWN_DECK_COUNT, 35.336);
		v.set(WeightedFeature.OPPONENT_DECK_COUNT, -84.377);
		v.set(WeightedFeature.OWN_SECRET_COUNT, 20.0);
		v.set(WeightedFeature.OPPONENT_SECRET_COUNT, -20.0);
		v.set(WeightedFeature.LOCKED_MANA_VALUE, -43.195);
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 16.063);
		v.set(WeightedFeature.CORPSE_COUNT_VALUE, 18.548);
		// Board width
		v.set(WeightedFeature.OWN_MINION_COUNT, 78.758);
		v.set(WeightedFeature.OPPONENT_MINION_COUNT, -66.374);
		// Opponent minion stats
		v.set(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE, -94.884);
		v.set(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR, -71.455);
		v.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -10.597);
		// Opponent minion keywords
		v.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, -53.569);
		v.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, -27.547);
		v.set(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER, -39.826);
		v.set(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER, -51.586);
		v.set(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER, -35.722);
		v.set(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER, -68.364);
		v.set(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER, -28.510);
		v.set(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, -69.403);
		v.set(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER, -16.061);
		v.set(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER, -49.679);
		v.set(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER, -93.791);
		v.set(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER, 25.979);
		v.set(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER, -11.306);
		v.set(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER, -37.503);
		v.set(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER, -51.299);
		v.set(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER, 27.345);
		// Opponent weapon/armor
		v.set(WeightedFeature.OPPONENT_WEAPON_VALUE, -30.829);
		v.set(WeightedFeature.OPPONENT_ARMOR_FACTOR, -11.655);
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
