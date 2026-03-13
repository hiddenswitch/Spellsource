package com.hiddenswitch.spellsource.trainer;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.heuristic.FeatureVector;
import net.demilich.metastone.game.behaviour.heuristic.WeightedFeature;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.statistics.Statistic;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Evaluates a candidate FeatureVector by playing games against a Hall of Fame
 * of historically strong opponents.
 * <p>
 * Based on the competitive coevolution approach from García-Sánchez &amp; Tonda
 * (2019) "Optimizing Hearthstone agents using an evolutionary algorithm",
 * adapted for CMA-ES where we cannot do full round-robin within a generation.
 * Instead, we use a Hall of Fame archive (Rosin &amp; Belew 1997) as the opponent set.
 * <p>
 * Matchups are distributed across HoF members and deck pairs. The total game
 * budget stays constant regardless of HoF size — more opponents means fewer
 * games per opponent, but broader coverage.
 * <p>
 * All games are parallelized across available cores.
 */
public class FitnessEvaluator {
	private static final Logger LOG = Logger.getLogger(FitnessEvaluator.class.getName());

	private final int gsvbDepth;
	private final int gsvbTimeout;
	private final long gameTimeoutMs;
	private MlflowReporter mlflow;

	public FitnessEvaluator(int gsvbDepth, int gsvbTimeout, long gameTimeoutMs) {
		this.gsvbDepth = gsvbDepth;
		this.gsvbTimeout = gsvbTimeout;
		this.gameTimeoutMs = gameTimeoutMs;
	}

	public FitnessEvaluator(int gsvbDepth, int gsvbTimeout) {
		this(gsvbDepth, gsvbTimeout, 90_000);
	}

	public void setMlflow(MlflowReporter mlflow) {
		this.mlflow = mlflow;
	}

	/**
	 * A single game task: deck pair, opponent weights, who is candidate (P1 or P2).
	 */
	private record GameTask(GameDeck deck1, GameDeck deck2, FeatureVector opponent, boolean candidateIsPlayer1,
	                         int gameIndex) {
	}

	/**
	 * Evaluates a candidate against a Hall of Fame of opponents.
	 * <p>
	 * Matchups are distributed across all HoF members: each matchup is assigned
	 * to a random HoF member, so the candidate faces all of them across the
	 * evaluation. Total game count = matchupsToSample * gamesPerMatchup,
	 * same budget regardless of HoF size.
	 *
	 * @param opponents the Hall of Fame members to play against
	 */
	public EvalResult evaluate(String id, FeatureVector candidate, List<FeatureVector> opponents,
	                           List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup) {
		long start = System.currentTimeMillis();
		Random rng = new Random();

		// Sample random non-mirror deck pairs
		List<int[]> matchups = sampleMatchups(allDecks.size(), matchupsToSample, rng);

		// Flatten all games across all matchups into a single list
		// Each matchup is assigned to a random opponent from the HoF
		List<GameTask> allTasks = new ArrayList<>();
		int gameIndex = 0;
		for (int[] matchup : matchups) {
			GameDeck deck1 = allDecks.get(matchup[0]);
			GameDeck deck2 = allDecks.get(matchup[1]);
			FeatureVector opponent = opponents.get(rng.nextInt(opponents.size()));

			int gamesAsP1 = gamesPerMatchup / 2;
			int gamesAsP2 = gamesPerMatchup - gamesAsP1;

			for (int i = 0; i < gamesAsP1; i++) {
				allTasks.add(new GameTask(deck1, deck2, opponent, true, gameIndex++));
			}
			for (int i = 0; i < gamesAsP2; i++) {
				allTasks.add(new GameTask(deck1, deck2, opponent, false, gameIndex++));
			}
		}

		// Run all games in parallel
		AtomicInteger wins = new AtomicInteger(0);
		AtomicInteger completed = new AtomicInteger(0);
		AtomicInteger timeouts = new AtomicInteger(0);

		allTasks.parallelStream().forEach(task -> {
			try {
				FeatureVector p1Weights = task.candidateIsPlayer1 ? candidate : task.opponent;
				FeatureVector p2Weights = task.candidateIsPlayer1 ? task.opponent : candidate;

				GameContext game = GameContext.fromDecks(List.of(task.deck1, task.deck2));
				game.setBehaviour(0, createBehaviour(p1Weights));
				game.setBehaviour(1, createBehaviour(p2Weights));

				game.init();

				// Run with per-game timeout
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Future<?> future = executor.submit(() -> game.resume());
				try {
					future.get(gameTimeoutMs, TimeUnit.MILLISECONDS);
				} catch (TimeoutException e) {
					future.cancel(true);
					executor.shutdownNow();
					try {
						executor.awaitTermination(2, TimeUnit.SECONDS);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
					timeouts.incrementAndGet();
					LOG.info(String.format("Game timeout: %s game %d (%s vs %s) at turn %d",
							id, task.gameIndex,
							task.deck1.getName(), task.deck2.getName(),
							game.getTurn()));
					if (mlflow != null) {
						try {
							String trace = game.getTrace().dump();
							mlflow.logTimeoutTrace(id, task.gameIndex, trace);
						} catch (Exception traceEx) {
							// best effort
						}
					}
					completed.incrementAndGet();
					return;
				} finally {
					executor.shutdownNow();
				}

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

		String timeoutStr = timeouts.get() > 0 ? String.format(", %d timeouts", timeouts.get()) : "";
		LOG.info(String.format("Evaluated %s: %.1f%% win rate (%d/%d games, %d HoF opponents%s) in %ds",
				id, winRate * 100, wins.get(), totalGames, opponents.size(), timeoutStr, duration / 1000));

		return new EvalResult(id, winRate, totalGames, duration);
	}

	/**
	 * Convenience overload for single-opponent evaluation (used by distributed workers).
	 */
	public EvalResult evaluate(String id, FeatureVector candidate, FeatureVector baseline,
	                           List<GameDeck> allDecks, int matchupsToSample, int gamesPerMatchup) {
		return evaluate(id, candidate, List.of(baseline), allDecks, matchupsToSample, gamesPerMatchup);
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
