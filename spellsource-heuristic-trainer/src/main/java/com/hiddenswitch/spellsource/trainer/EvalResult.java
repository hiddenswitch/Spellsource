package com.hiddenswitch.spellsource.trainer;

public record EvalResult(
		String id,
		double winRate,
		int gamesPlayed,
		long durationMs
) {
}
