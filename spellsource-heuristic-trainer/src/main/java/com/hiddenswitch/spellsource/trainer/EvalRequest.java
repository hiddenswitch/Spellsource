package com.hiddenswitch.spellsource.trainer;

import java.util.Map;

public record EvalRequest(
		String id,
		int generation,
		Map<String, Double> weights,
		Map<String, Double> baselineWeights,
		int matchupsToSample,
		int gamesPerMatchup,
		int gsvbDepth,
		int gsvbTimeout
) {
}
