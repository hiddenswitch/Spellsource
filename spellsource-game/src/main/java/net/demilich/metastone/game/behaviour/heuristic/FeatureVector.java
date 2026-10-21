package net.demilich.metastone.game.behaviour.heuristic;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Values recorded for the default and optimized values for {@link net.demilich.metastone.game.behaviour.GameStateValueBehaviour}'s
 * best heuristic weights.
 */
public class FeatureVector implements Cloneable, Serializable {

	/**
	 * Returns the published {@link FeatureVector} loaded from a trusted MLflow run if
	 * {@code SPELLSOURCE_MLFLOW_TRACKING_URI} / {@code MLFLOW_TRACKING_URI} is configured, or falls back to
	 * {@link #getFittest()}.
	 */
	public static FeatureVector getPublished() {
		return MlflowFeatureVectorLoader.getOrLoadPublished();
	}

	/**
	 * Explicitly loads a {@link FeatureVector} from a trusted/published MLflow run, validating and projecting weights
	 * through {@link FeatureVectorConstraints}.
	 */
	public static Optional<FeatureVector> loadFromTrustedMlflowRun(String trackingUri, String experimentName, String trustedRunId) {
		return MlflowFeatureVectorLoader.loadFromTrustedMlflowRun(trackingUri, experimentName, trustedRunId);
	}

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
		defaultVector.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -1.5);
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
		defaultVector.set(WeightedFeature.QUEST_COUNTER_VALUE, 10);
		defaultVector.set(WeightedFeature.QUEST_REWARD_VALUE, 20);
		// Mana crystals
		defaultVector.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 8.5);
		defaultVector.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -8.5);
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
		// Locations, Dormant, Handbuff/Mana discount, Imbue, Starships
		defaultVector.set(WeightedFeature.OWN_LOCATION_VALUE, 4);
		defaultVector.set(WeightedFeature.OPPONENT_LOCATION_VALUE, -4);
		defaultVector.set(WeightedFeature.OWN_DORMANT_VALUE, 1.5);
		defaultVector.set(WeightedFeature.OPPONENT_DORMANT_VALUE, -1.5);
		defaultVector.set(WeightedFeature.HAND_BUFF_VALUE, 0.8);
		defaultVector.set(WeightedFeature.HAND_MANA_REDUCTION_VALUE, 1.5);
		defaultVector.set(WeightedFeature.IMBUE_VALUE, 5);
		defaultVector.set(WeightedFeature.STARSHIP_VALUE, 3);
		return defaultVector;
	}

	public static FeatureVector getFittest() {
		FeatureVector v = new FeatureVector();
		// 7-island Multi-Node Parallel Log-Normal Genetic Algorithm (ga_lognormal_v8, 73-dim,
		// MLflow run a5516a5611a8422b8888b80336c8087f, seeded from FeatureVector.getDefault() with unit gauge OWN_HP_FACTOR=1.0).
		// Evaluated with Common Random Numbers (CRN) paired swap-side games and out-of-sample CRN validation gating.
		// Threat level
		v.set(WeightedFeature.RED_MODIFIER, -50.0);
		v.set(WeightedFeature.YELLOW_MODIFIER, -10.0);
		// Health
		v.set(WeightedFeature.OWN_HP_FACTOR, 1.0);
		v.set(WeightedFeature.OPPONENT_HP_FACTOR, -1.0);
		// Card advantage
		v.set(WeightedFeature.OWN_CARD_COUNT, 3.243);
		v.set(WeightedFeature.OPPONENT_CARD_COUNT, -2.181);
		// Own minion stats
		v.set(WeightedFeature.MINION_INTRINSIC_VALUE, 1.0);
		v.set(WeightedFeature.MINION_ATTACK_FACTOR, 2.243);
		v.set(WeightedFeature.MINION_HP_FACTOR, 1.598);
		// Own minion keywords
		v.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, 10.0);
		v.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, 4.0);
		v.set(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER, 2.0);
		v.set(WeightedFeature.MINION_WINDFURY_MODIFIER, 6.0);
		v.set(WeightedFeature.MINION_DIVINE_SHIELD_MODIFIER, 4.423);
		v.set(WeightedFeature.MINION_SPELL_POWER_MODIFIER, 3.0);
		v.set(WeightedFeature.MINION_STEALTHED_MODIFIER, 3.0);
		v.set(WeightedFeature.MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, 3.0);
		v.set(WeightedFeature.MINION_POISONOUS_MODIFIER, 5.0);
		v.set(WeightedFeature.MINION_LIFESTEAL_MODIFIER, 5.273);
		v.set(WeightedFeature.MINION_REBORN_MODIFIER, 3.786);
		v.set(WeightedFeature.MINION_FROZEN_MODIFIER, -4.529);
		v.set(WeightedFeature.MINION_DEATHRATTLE_MODIFIER, 2.167);
		v.set(WeightedFeature.MINION_RUSH_MODIFIER, 0.3);
		v.set(WeightedFeature.MINION_IMMUNE_MODIFIER, 8.0);
		v.set(WeightedFeature.MINION_CANNOT_ATTACK_MODIFIER, -5.0);
		// Hand/deck/resource
		v.set(WeightedFeature.CURSED_FACTOR, -29.083);
		v.set(WeightedFeature.HARD_REMOVAL_VALUE, 2.0);
		v.set(WeightedFeature.QUEST_COUNTER_VALUE, 18.0);
		v.set(WeightedFeature.QUEST_REWARD_VALUE, 36.0);
		v.set(WeightedFeature.EMPTY_MANA_CRYSTAL_VALUE, 13.5);
		v.set(WeightedFeature.OPPOSING_EMPTY_MANA_CRYSTAL_VALUE, -8.5);
		v.set(WeightedFeature.OWN_ROASTED_VALUE, -12.363);
		v.set(WeightedFeature.OPPONENT_ROASTED_VALUE, 17.892);
		v.set(WeightedFeature.OWN_ARMOR_FACTOR, 1.079);
		v.set(WeightedFeature.WEAPON_VALUE, 3.0);
		v.set(WeightedFeature.OWN_DECK_COUNT, 0.1);
		v.set(WeightedFeature.OPPONENT_DECK_COUNT, -0.1);
		v.set(WeightedFeature.OWN_SECRET_COUNT, 3.0);
		v.set(WeightedFeature.OPPONENT_SECRET_COUNT, -3.0);
		v.set(WeightedFeature.LOCKED_MANA_VALUE, -5.0);
		v.set(WeightedFeature.OPPONENT_LOCKED_MANA_VALUE, 5.0);
		v.set(WeightedFeature.CORPSE_COUNT_VALUE, 1.25);
		// Board width
		v.set(WeightedFeature.OWN_MINION_COUNT, 0.923);
		v.set(WeightedFeature.OPPONENT_MINION_COUNT, -1.284);
		// Opponent minion stats
		v.set(WeightedFeature.OPPONENT_MINION_INTRINSIC_VALUE, -1.0);
		v.set(WeightedFeature.OPPONENT_MINION_ATTACK_FACTOR, -2.22);
		v.set(WeightedFeature.OPPONENT_MINION_HP_FACTOR, -3.567);
		// Opponent minion keywords
		v.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, -4.0);
		v.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, -2.0);
		v.set(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER, -0.816);
		v.set(WeightedFeature.OPPONENT_MINION_WINDFURY_MODIFIER, -6.0);
		v.set(WeightedFeature.OPPONENT_MINION_DIVINE_SHIELD_MODIFIER, -5.0);
		v.set(WeightedFeature.OPPONENT_MINION_SPELL_POWER_MODIFIER, -3.0);
		v.set(WeightedFeature.OPPONENT_MINION_STEALTHED_MODIFIER, -3.87);
		v.set(WeightedFeature.OPPONENT_MINION_UNTARGETABLE_BY_SPELLS_MODIFIER, -4.0);
		v.set(WeightedFeature.OPPONENT_MINION_POISONOUS_MODIFIER, -3.0);
		v.set(WeightedFeature.OPPONENT_MINION_LIFESTEAL_MODIFIER, -2.0);
		v.set(WeightedFeature.OPPONENT_MINION_REBORN_MODIFIER, -1.621);
		v.set(WeightedFeature.OPPONENT_MINION_FROZEN_MODIFIER, 3.0);
		v.set(WeightedFeature.OPPONENT_MINION_DEATHRATTLE_MODIFIER, -2.0);
		v.set(WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER, -0.333);
		v.set(WeightedFeature.OPPONENT_MINION_IMMUNE_MODIFIER, -10.0);
		v.set(WeightedFeature.OPPONENT_MINION_CANNOT_ATTACK_MODIFIER, 3.0);
		// Opponent weapon/armor
		v.set(WeightedFeature.OPPONENT_WEAPON_VALUE, -3.405);
		v.set(WeightedFeature.OPPONENT_ARMOR_FACTOR, -0.5);
		// Locations, Dormant, Handbuff/Mana discount, Imbue, Starships
		v.set(WeightedFeature.OWN_LOCATION_VALUE, 3.123);
		v.set(WeightedFeature.OPPONENT_LOCATION_VALUE, -3.869);
		v.set(WeightedFeature.OWN_DORMANT_VALUE, 1.307);
		v.set(WeightedFeature.OPPONENT_DORMANT_VALUE, -1.425);
		v.set(WeightedFeature.HAND_BUFF_VALUE, 1.516);
		v.set(WeightedFeature.HAND_MANA_REDUCTION_VALUE, 3.281);
		v.set(WeightedFeature.IMBUE_VALUE, 4.839);
		v.set(WeightedFeature.STARSHIP_VALUE, 7.5);
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
