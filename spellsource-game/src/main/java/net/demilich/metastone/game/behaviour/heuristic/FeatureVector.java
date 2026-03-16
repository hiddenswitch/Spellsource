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
		// 7-island IPOP-CMA-ES (ipop_cmaes_hof_v2, 65-dim). For each feature, the
		// raw trained value from the highest-WR island with a sensible sign is used
		// unmodified. Source island noted in comment (I2/I3=87.5%, I4=85.8%, etc).
		// Rare features (0-3 deck instances) use sensible defaults.
		// Threat level
		v.set(WeightedFeature.RED_MODIFIER, -35.281);                          // I2
		v.set(WeightedFeature.YELLOW_MODIFIER, -64.756);                       // I2
		// Health
		v.set(WeightedFeature.OWN_HP_FACTOR, 41.294);                          // I2
		v.set(WeightedFeature.OPPONENT_HP_FACTOR, -53.548);                     // I2
		// Card advantage
		v.set(WeightedFeature.OWN_CARD_COUNT, 5.891);                          // I2
		v.set(WeightedFeature.OPPONENT_CARD_COUNT, -39.950);                    // I2
		// Own minion stats
		v.set(WeightedFeature.MINION_INTRINSIC_VALUE, 12.119);                 // I3
		v.set(WeightedFeature.MINION_ATTACK_FACTOR, 31.775);                   // I2
		v.set(WeightedFeature.MINION_HP_FACTOR, 66.084);                       // I2
		// Own minion keywords
		v.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, -43.848);             // I2 (7/7 negative)
		v.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, 57.246);           // I2
		v.set(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER, 26.369);          // I2
		v.set(WeightedFeature.MINION_WINDFURY_MODIFIER, 30.0);                 // rare (3 cards)
		v.set(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER, 57.090);          // I4
		v.set(WeightedFeature.MINION_SPELL_POWER_MODIFIER, 19.895);            // I4
		v.set(WeightedFeature.MINION_STEALTHED_MODIFIER, 32.0);                // rare (2 cards)
		v.set(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, 5.678);  // I4
		v.set(WeightedFeature.MINION_POISONOUS_MODIFIER, 56.952);              // I2
		v.set(WeightedFeature.MINION_LIFESTEAL_MODIFIER, 20.959);              // I3
		v.set(WeightedFeature.MINION_REBORN_MODIFIER, 30.845);                 // I2
		v.set(WeightedFeature.MINION_FROZEN_MODIFIER, -2.830);                 // I2
		v.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, -7.499);            // I2 (6/7 negative)
		v.set(WeightedFeature.MINION_RUSH_MODIFIER, -17.642);                  // I3 (5/7 negative)
		v.set(WeightedFeature.MINION_IMMUNE_MODIFIER, 27.0);                   // rare (3 cards)
		v.set(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER, -20.751);         // I3
		// Hand/deck/resource
		v.set(WeightedFeature.CURSED_FACTOR, -26.327);                         // I4
		v.set(WeightedFeature.HARD_REMOVAL_VALUE, 5.422);                      // I2
		v.set(WeightedFeature.QUEST_COUNTER_VALUE, 3.0);                       // default (7/7 negative)
		v.set(WeightedFeature.QUEST_REWARD_VALUE, 4.937);                      // I2
		v.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 60.907);               // I2
		v.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -26.964);     // I5
		v.set(WeightedFeature.OWN_ROASTED_VALUE, -27.101);                     // I2
		v.set(WeightedFeature.OPPONENT_ROASTED_VALUE, 57.639);                 // I3
		v.set(WeightedFeature.OWN_ARMOR_FACTOR, 18.628);                       // I3
		v.set(WeightedFeature.WEAPON_VALUE, 71.682);                           // I3
		v.set(WeightedFeature.OWN_DECK_COUNT, 3.683);                          // I2
		v.set(WeightedFeature.OPPONENT_DECK_COUNT, -57.584);                   // I2
		v.set(WeightedFeature.OWN_SECRET_COUNT, 20.0);                         // rare (0 secret decks)
		v.set(WeightedFeature.OPPONENT_SECRET_COUNT, -20.0);                   // rare (0 secret decks)
		v.set(WeightedFeature.LOCKED_MANA_VALUE, -29.993);                     // I2
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 95.209);             // I3
		v.set(WeightedFeature.CORPSE_COUNT_VALUE, 3.732);                      // I2
		// Board width
		v.set(WeightedFeature.OWN_MINION_COUNT, 17.029);                       // I2
		v.set(WeightedFeature.OPPONENT_MINION_COUNT, -87.969);                  // I2
		// Opponent minion stats
		v.set(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE, -94.884);        // I4
		v.set(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR, -28.155);          // I2
		v.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -13.626);              // I3
		// Opponent minion keywords
		v.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, -48.451);     // I3
		v.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, -27.547);  // I4
		v.set(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER, -36.627); // I2
		v.set(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER, -22.0);        // rare (3 cards)
		v.set(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER, -29.292); // I2
		v.set(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER, -29.192);   // I2
		v.set(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER, -20.0);       // rare (2 cards)
		v.set(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, -89.848); // I3
		v.set(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER, -51.953);     // I1
		v.set(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER, -85.700);     // I2
		v.set(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER, -30.119);        // I2
		v.set(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER, 34.888);         // I2
		v.set(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER, -25.806);   // I2
		v.set(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER, -22.734);          // I2
		v.set(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER, -50.0);          // rare (3 cards)
		v.set(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER, 31.366);  // I3
		// Opponent weapon/armor
		v.set(WeightedFeature.OPPONENT_WEAPON_VALUE, -59.022);                  // I0
		v.set(WeightedFeature.OPPONENT_ARMOR_FACTOR, -37.042);                  // I2
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 95.209);              // I3
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
