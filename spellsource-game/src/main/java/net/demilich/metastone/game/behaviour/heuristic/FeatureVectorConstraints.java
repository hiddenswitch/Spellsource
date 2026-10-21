package net.demilich.metastone.game.behaviour.heuristic;

/**
 * Enforces basic numerical safety, sign polarity (matching {@link FeatureVector#getDefault()}), and threat-tier
 * monotonicity (`RED` vs `YELLOW` vs `DEFAULT`) on {@link FeatureVector} weights.
 * <p>
 * Relative feature magnitudes (such as minion attack/HP trade ratios, card advantage, weapon valuation, and modern
 * mechanic weights) are not artificially clamped to fixed ratios; instead, they emerge naturally from multi-generation
 * evolutionary self-play in {@code ParallelGeneticTrainer}.
 */
public final class FeatureVectorConstraints {

	private static final FeatureVector DEFAULT_POLARITY_REF = FeatureVector.getDefault();

	private FeatureVectorConstraints() {
	}

	/**
	 * Projects a raw weight array (indexed by {@link WeightedFeature#ordinal()}) in-place onto the valid sign-polarity
	 * domain and returns it.
	 */
	public static double[] project(double[] weights) {
		FeatureVector fv = new FeatureVector();
		WeightedFeature[] features = WeightedFeature.values();
		int len = Math.min(weights.length, features.length);
		for (int i = 0; i < len; i++) {
			fv.set(features[i], weights[i]);
		}
		project(fv);
		for (int i = 0; i < len; i++) {
			weights[i] = fv.get(features[i]);
		}
		return weights;
	}

	/**
	 * Projects a {@link FeatureVector} in-place onto the valid sign-polarity and threat-monotonicity domain.
	 */
	public static FeatureVector project(FeatureVector v) {
		for (WeightedFeature f : WeightedFeature.values()) {
			double defaultVal = DEFAULT_POLARITY_REF.get(f);
			double val = v.get(f);
			if (!Double.isFinite(val)) {
				val = defaultVal;
			}
			val = Math.max(-500.0, Math.min(500.0, val));

			// Preserve fundamental sign polarity relative to FeatureVector.getDefault()
			double minMag = Math.max(0.05, Math.abs(defaultVal) * 0.1);
			if (defaultVal > 0 && val < minMag) {
				val = minMag;
			} else if (defaultVal < 0 && val > -minMag) {
				val = -minMag;
			} else if (defaultVal == 0 && f == WeightedFeature.OPPONENT_MINION_RUSH_MODIFIER && val > 0.0) {
				val = 0.0;
			}
			v.set(f, val);
		}

		// Threat-tier monotonicity: RED threat is at least as severe as YELLOW threat,
		// and Taunt under RED threat is at least as valuable as under YELLOW, which is at least as valuable as DEFAULT.
		if (v.get(WeightedFeature.RED_MODIFIER) > v.get(WeightedFeature.YELLOW_MODIFIER)) {
			v.set(WeightedFeature.RED_MODIFIER, v.get(WeightedFeature.YELLOW_MODIFIER));
		}
		if (v.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER) < v.get(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER)) {
			v.set(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER, v.get(WeightedFeature.MINION_DEFAULT_TAUNT_MODIFIER));
		}
		if (v.get(WeightedFeature.MINION_RED_TAUNT_MODIFIER) < v.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER)) {
			v.set(WeightedFeature.MINION_RED_TAUNT_MODIFIER, v.get(WeightedFeature.MINION_YELLOW_TAUNT_MODIFIER));
		}
		if (v.get(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER) > v.get(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER)) {
			v.set(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER, v.get(WeightedFeature.OPPONENT_MINION_DEFAULT_TAUNT_MODIFIER));
		}
		if (v.get(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER) > v.get(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER)) {
			v.set(WeightedFeature.OPPONENT_MINION_RED_TAUNT_MODIFIER, v.get(WeightedFeature.OPPONENT_MINION_YELLOW_TAUNT_MODIFIER));
		}

		return v;
	}
}
