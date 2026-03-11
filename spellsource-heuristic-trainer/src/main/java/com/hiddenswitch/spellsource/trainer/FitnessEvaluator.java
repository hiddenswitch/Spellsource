package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.SimulationResult;
import net.demilich.metastone.game.statistics.Statistic;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Evaluates a candidate FeatureVector by playing games against a baseline.
 * All matchups and games are parallelized across available cores.
 */
public class FitnessEvaluator {
	private static final Logger LOG = Logger.getLogger(FitnessEvaluator.class.getName());

	private final int gsvbDepth;
	private final int gsvbTimeout;

	public FitnessEvaluator(int gsvbDepth, int gsvbTimeout) {
		this.gsvbDepth = gsvbDepth;
		this.gsvbTimeout = gsvbTimeout;
	}

	/**
	 * A single game task: deck pair, who is candidate (P1 or P2).
	 */
	private record GameTask(GameDeck deck1, GameDeck deck2, boolean candidateIsPlayer1) {
	}

	/**
	 * Evaluates a candidate against a baseline by playing sampled matchups.
	 * All games across all matchups are flattened into a single parallel stream.
	 */
	public EvalResult evaluate(String id, FeatureVector candidate, FeatureVector baseline,
	                           List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup) {
		long start = System.currentTimeMillis();
		Random rng = new Random();

		// Sample random non-mirror deck pairs
		List<int[]> matchups = sampleMatchups(allDecks.size(), matchupsToSample, rng);

		// Flatten all games across all matchups into a single list
		List<GameTask> allTasks = new ArrayList<>();
		for (int[] matchup : matchups) {
			GameDeck deck1 = allDecks.get(matchup[0]);
			GameDeck deck2 = allDecks.get(matchup[1]);

			int gamesAsP1 = gamesPerMatchup / 2;
			int gamesAsP2 = gamesPerMatchup - gamesAsP1;

			for (int i = 0; i < gamesAsP1; i++) {
				allTasks.add(new GameTask(deck1, deck2, true));
			}
			for (int i = 0; i < gamesAsP2; i++) {
				allTasks.add(new GameTask(deck1, deck2, false));
			}
		}

		// Run all games in parallel
		AtomicInteger wins = new AtomicInteger(0);
		AtomicInteger completed = new AtomicInteger(0);

		allTasks.parallelStream().forEach(task -> {
			try {
				FeatureVector p1Weights = task.candidateIsPlayer1 ? candidate : baseline;
				FeatureVector p2Weights = task.candidateIsPlayer1 ? baseline : candidate;

				GameContext game = GameContext.fromDecks(List.of(task.deck1, task.deck2));
				game.setBehaviour(0, createBehaviour(p1Weights));
				game.setBehaviour(1, createBehaviour(p2Weights));

				game.init();
				game.resume();

				boolean candidateWon;
				if (task.candidateIsPlayer1) {
					candidateWon = game.getPlayer1().getStatistics().getLong(Statistic.GAMES_WON) > 0;
				} else {
					candidateWon = game.getPlayer2().getStatistics().getLong(Statistic.GAMES_WON) > 0;
				}
				if (candidateWon) {
					wins.incrementAndGet();
				}
			} catch (Throwable t) {
				// Count as a loss
			}
			completed.incrementAndGet();
		});

		int totalGames = completed.get();
		double winRate = totalGames > 0 ? (double) wins.get() / totalGames : 0.5;
		long duration = System.currentTimeMillis() - start;

		LOG.info(String.format("Evaluated %s: %.1f%% win rate (%d/%d games) in %ds",
				id, winRate * 100, wins.get(), totalGames, duration / 1000));

		return new EvalResult(id, winRate, totalGames, duration);
	}

	private GameStateValueBehaviour createBehaviour(FeatureVector weights) {
		GameStateValueBehaviour gsvb = new GameStateValueBehaviour(weights, "Trainer");
		gsvb.setMaxDepth(gsvbDepth);
		gsvb.setTimeout(gsvbTimeout);
		gsvb.setThrowsExceptions(false);
		return gsvb;
	}

	private List<int[]> sampleMatchups(int deckCount, int numMatchups, Random rng) {
		List<int[]> allPairs = new ArrayList<>();
		for (int i = 0; i < deckCount; i++) {
			for (int j = i + 1; j < deckCount; j++) {
				allPairs.add(new int[]{i, j});
			}
		}

		if (allPairs.size() <= numMatchups) {
			return allPairs;
		}

		Collections.shuffle(allPairs, rng);
		return allPairs.subList(0, numMatchups);
	}

	public int getGsvbDepth() {
		return gsvbDepth;
	}

	public int getGsvbTimeout() {
		return gsvbTimeout;
	}

	/**
	 * Creates a FeatureVector from a weight map (feature name → value).
	 */
	public static FeatureVector fromWeightMap(Map<String, Double> weights) {
		FeatureVector fv = new FeatureVector();
		for (var entry : weights.entrySet()) {
			WeightedFeature feature = WeightedFeature.valueOf(entry.getKey());
			fv.set(feature, entry.getValue());
		}
		return fv;
	}

	/**
	 * Converts a FeatureVector to a weight map (feature name → value).
	 */
	public static Map<String, Double> toWeightMap(FeatureVector fv) {
		Map<String, Double> map = new LinkedHashMap<>();
		for (WeightedFeature feature : WeightedFeature.values()) {
			map.put(feature.name(), fv.get(feature));
		}
		return map;
	}
}
